"""Fills image.direct_image_url in indoor_plants_india.json with direct Wikimedia image links.
Usage:  python resolve_images.py            (just fill links)
        python resolve_images.py --download images   (also save the image files)
Images come from Wikipedia/Wikimedia Commons; check each file's licence on its Commons page before commercial use."""
import json, sys, os, urllib.request

UA = {"User-Agent": "IndoorPlantsIndiaApp/1.0 (contact: you@example.com)"}  # put your own contact
def get(url):
    return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=30).read()

path = "indoor_plants_india.json"
data = json.load(open(path, encoding="utf-8"))
dl = sys.argv[sys.argv.index("--download") + 1] if "--download" in sys.argv else None
if dl: os.makedirs(dl, exist_ok=True)

for p in data["plants"]:
    img = p["image"]
    try:
        info = json.loads(get(img["resolver_api_url"]))
        url = info["originalimage"]["source"]
        img["direct_image_url"] = url
        img["thumbnail_url"] = info.get("thumbnail", {}).get("source")
        if dl:
            ext = os.path.splitext(url.split("?")[0])[1] or ".jpg"
            fn = os.path.join(dl, f"{p['id']:02d}_{p['common_name'].lower().replace(' ', '_')}{ext}")
            open(fn, "wb").write(get(url)); img["local_file"] = fn
        print("OK ", p["common_name"], url)
    except Exception as e:
        print("FAIL", p["common_name"], e)

json.dump(data, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
