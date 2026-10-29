"""
Download all plant images (IDs 46 through 220) into app/src/main/assets/images/
Reads all section files in app/src/main/assets/section_*.json.
Prefers thumbnail_url (330px/640px, ~40KB) for mobile app efficiency, falling back to direct_image_url,
and falling back to resolver_api_url if static links fail.

Usage:
  python3 download_all_images.py
  or
  python3 download_all_images.py --force (re-download existing)
"""

import json
import glob
import os
import sys
import time
import urllib.request
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
IMAGES_DIR = os.path.join(SCRIPT_DIR, "images")
os.makedirs(IMAGES_DIR, exist_ok=True)

USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 (HappyPlantsApp/1.0; contact: botany@happyplants.app)"
HEADERS = {
    "User-Agent": USER_AGENT,
    "Accept": "image/avif,image/webp,image/apng,image/*,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
}

def fetch_url(url, timeout=25):
    # Ensure URL is properly quoted if needed
    parsed = urllib.parse.urlsplit(url)
    encoded_path = urllib.parse.quote(urllib.parse.unquote(parsed.path), safe="/:@!$&'()*+,;=-")
    clean_url = urllib.parse.urlunsplit((parsed.scheme, parsed.netloc, encoded_path, parsed.query, parsed.fragment))
    
    req = urllib.request.Request(clean_url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.read()

def resolve_fresh_urls(resolver_url):
    try:
        data = fetch_url(resolver_url, timeout=15)
        info = json.loads(data)
        orig = info.get("originalimage", {}).get("source")
        thumb = info.get("thumbnail", {}).get("source")
        res = []
        if thumb: res.append(thumb)
        if orig: res.append(orig)
        return res
    except Exception:
        return []

def download_plant_image(plant, force=False):
    pid = plant.get("id")
    common_name = plant.get("common_name", "Unknown")
    img_info = plant.get("image", {})
    
    local_file = img_info.get("local_file")
    if not local_file:
        return (pid, common_name, "SKIPPED_NO_FILE", 0)
    
    filename = os.path.basename(local_file)
    dest_path = os.path.join(IMAGES_DIR, filename)
    
    if os.path.exists(dest_path) and os.path.getsize(dest_path) > 1000 and not force:
        return (pid, common_name, "SKIPPED_EXISTING", os.path.getsize(dest_path))
    
    urls = []
    if img_info.get("thumbnail_url"):
        urls.append(img_info["thumbnail_url"])
    if img_info.get("direct_image_url"):
        urls.append(img_info["direct_image_url"])
    
    # Try static URLs
    for u in urls:
        try:
            content = fetch_url(u)
            if len(content) > 500:
                with open(dest_path, "wb") as f:
                    f.write(content)
                return (pid, common_name, "DOWNLOADED", len(content))
        except Exception:
            pass
        time.sleep(0.1)

    # Fallback: Query resolver_api_url if available
    resolver_url = img_info.get("resolver_api_url")
    if resolver_url:
        fresh_urls = resolve_fresh_urls(resolver_url)
        for u in fresh_urls:
            try:
                content = fetch_url(u)
                if len(content) > 500:
                    with open(dest_path, "wb") as f:
                        f.write(content)
                    return (pid, common_name, "DOWNLOADED_RESOLVED", len(content))
            except Exception:
                pass
            time.sleep(0.1)

    return (pid, common_name, "FAILED", 0)

def main():
    force = "--force" in sys.argv
    section_files = sorted(glob.glob(os.path.join(SCRIPT_DIR, "section_*.json")))
    
    all_plants = []
    for sf in section_files:
        with open(sf, "r", encoding="utf-8") as f:
            data = json.load(f)
        plants = data.get("plants", []) if isinstance(data, dict) else data
        all_plants.extend(plants)
    
    all_plants.sort(key=lambda p: p.get("id", 0))
    print(f"Total plants discovered across sections: {len(all_plants)}")
    
    to_download = []
    for p in all_plants:
        local_file = p.get("image", {}).get("local_file")
        if not local_file: continue
        fn = os.path.basename(local_file)
        dp = os.path.join(IMAGES_DIR, fn)
        if force or not os.path.exists(dp) or os.path.getsize(dp) <= 1000:
            to_download.append(p)
    
    print(f"Plants needing image download: {len(to_download)}")
    
    downloaded = 0
    failed = []
    
    # Use 4 parallel workers with a slight stagger
    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = {executor.submit(download_plant_image, plant, force): plant for plant in to_download}
        for future in as_completed(futures):
            pid, cname, status, size = future.result()
            if status in ("DOWNLOADED", "DOWNLOADED_RESOLVED"):
                downloaded += 1
                print(f"[{pid:03d}] OK: {cname} ({size // 1024} KB)")
            elif status == "FAILED":
                failed.append((pid, cname))
                print(f"[{pid:03d}] FAIL: {cname}")
            time.sleep(0.05)

    print("\n" + "=" * 50)
    print("Download Summary:")
    print(f"  Total plants in sections: {len(all_plants)}")
    print(f"  Already existed:          {len(all_plants) - len(to_download)}")
    print(f"  Successfully downloaded: {downloaded}")
    print(f"  Failed downloads:        {len(failed)}")
    if failed:
        print("\nFailed Plants:")
        for pid, cname in failed:
            print(f"  [{pid:03d}] {cname}")

if __name__ == "__main__":
    main()
