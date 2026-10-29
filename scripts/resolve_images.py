"""Fills image.direct_image_url in indoor_plants_india.json with direct Wikimedia image links.
Usage:  python resolve_images.py            (just fill links)
        python resolve_images.py --download images   (also save the image files)
Images come from Wikipedia/Wikimedia Commons; check each file's licence on its Commons page before commercial use."""
import json, sys, os, re, urllib.request

UA = {"User-Agent": "HappyPlantsApp/1.0 (contact: info@happyplants.app)"}
def get(url):
    return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=30).read()

path = "indoor_plants_india.json"
if not os.path.exists(path) and os.path.exists("app/src/main/assets/indoor_plants_india.json"):
    path = "app/src/main/assets/indoor_plants_india.json"

data = json.load(open(path, encoding="utf-8"))
dl = sys.argv[sys.argv.index("--download") + 1] if "--download" in sys.argv else None
if dl: os.makedirs(dl, exist_ok=True)

for p in data["plants"]:
    img = p["image"]
    try:
        info = json.loads(get(img["resolver_api_url"]))
        orig_url = info.get("originalimage", {}).get("source")
        thumb_url = info.get("thumbnail", {}).get("source")
        
        if orig_url: img["direct_image_url"] = orig_url
        if thumb_url: img["thumbnail_url"] = thumb_url
        
        download_url = thumb_url or orig_url
        if dl and download_url:
            clean_name = re.sub(r'[^a-zA-Z0-9]+', '_', p['common_name'].lower()).strip('_')
            ext = os.path.splitext(download_url.split("?")[0])[1] or ".jpg"
            filename = f"{p['id']:02d}_{clean_name}{ext}"
            fn = os.path.join(dl, filename)
            open(fn, "wb").write(get(download_url))
            img["local_file"] = f"images/{filename}"
            img["asset_path"] = f"file:///android_asset/images/{filename}"
            print(f"OK [{p['id']:02d}] {p['common_name']} -> {fn}")
        else:
            print(f"OK [{p['id']:02d}] {p['common_name']}")
    except Exception as e:
        print(f"FAIL [{p['id']:02d}] {p['common_name']}: {e}")

json.dump(data, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
print("Finished updating indoor_plants_india.json")

