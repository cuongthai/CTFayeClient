import ConfigParser
from functools import partial
from itertools import chain
import shutil
import os, time
import colors
import colorsys
import urllib2
import json
from PIL import Image
import numpy

APP_ID = "6d13fbf0-88e6-11e5-bf4e-178efd84da36"
FLURRY_API_KEY = "RFBTRTS7TJHTZBQT44R5"
ICON_LAUNCHER = "https://dl.dropboxusercontent.com/u/11028239/commerceassociates512.png"
VERSION_CODE = 1
VERSION_NAME = 0.9
LOGIN_METHOD = "custom"

CONFIG_FILE = "gradle.properties"
types = 'app/build-types'
src_package = "/res/values"

STRING_MAPPING = {"APP_NAME": "app_name",
                  "FACEBOOK_APP_ID": "fb_app_id",
                  "LOGIN_METHOD": "build_login_method",
                  "FLURRY_API_KEY": "flurry_api_key",
                  "ADMOBS_AD_UNIT": "banner_ad_unit_id"}

BOOL_MAPPING = {"OFFICIAL": "official",
                "ADS": "show_ads",
                "ALLOW_REGISTER": "allow_register",
                "SUPPORT_RSS": "support_rss",
                "SUPPORT_MUSIC_BOX": "support_music_box"}

SERVER_URL = "http://cloud.chatwing.com"
WHITE_LABEL_KEY_PATH = "../certs/whitelabel.keystore"
WHITE_LABEL_STORE_PASSWORD = "654321"
WHITE_LABEL_KEY_ALIAS = "whitelabel"
WHITE_LABEL_KEY_PASSWORD = "123456"


class Helper:
    def __init__(self, section, file):
        self.readline = partial(next, chain(("[{0}]\n".format(section),), file, ("",)))


def load_config():
    config = ConfigParser.RawConfigParser(allow_no_value=True)
    with open(CONFIG_FILE) as ifh:
        config.readfp(Helper("Config", ifh))
    return config


def get_value(config, key):
    return config.get("Config", key)


def ensure_build_type_folders():
    if os.path.exists(types):
        shutil.rmtree(types)

    os.mkdir(types)
    os.makedirs(types + "/release" + src_package)
    os.makedirs(types + "/debug" + src_package)


def write_string_xml():
    for type in ['/release', '/debug']:
        f = open(types + type + src_package + "/strings.xml", "w")
        f.writelines("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        f.writelines("<resources>\n")
        for appName in STRING_MAPPING:
            f.writelines("<string name=\"%s\">%s</string>\n" % (STRING_MAPPING[appName], get_value(config, appName)))
        f.writelines("</resources>\n")
        f.close()


def write_bool_xml():
    for type in ['/release', '/debug']:
        f = open(types + type + src_package + "/bools.xml", "w")
        f.writelines("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        f.writelines("<resources>\n")
        for key in BOOL_MAPPING:
            val = get_value(config, key)
            f.writelines("<bool name=\"%s\">%s</bool>\n" % (BOOL_MAPPING[key], "true" if "Yes" == val else "false"))
        f.writelines("</resources>\n")
        f.close()


def resize_square_image(filepath, outputfilepath, resolution):
    img = Image.open(filepath).convert('RGBA')
    premult = numpy.fromstring(img.tobytes(), dtype=numpy.uint8)
    alphaLayer = premult[3::4] / 255.0
    premult[::4] *= alphaLayer
    premult[1::4] *= alphaLayer
    premult[2::4] *= alphaLayer
    img = Image.frombytes("RGBA", img.size, premult.tostring())
    img.resize((resolution, resolution), Image.ANTIALIAS).save(outputfilepath)


def write_icons():
    import urllib2
    url = get_value(config, "IC_LAUNCHER")
    print url
    icon = urllib2.urlopen(url)
    output = open('ic_launcher.png', 'wb')
    output.write(icon.read())
    output.close()
    print "Wrote original image to root"

    dir_res = [("/drawable-hdpi/", 72), ("/drawable-xhdpi/", 96), ("/drawable-mdpi/", 48), ("/drawable-xxhdpi/", 144)]
    for build_type in ['/release', '/debug']:
        for (dir, res) in dir_res:
            print "Writing ic_launcher.png with res %s to %s/%s" % (res, build_type, dir)
            if not os.path.exists(types + build_type + "/res" + dir): os.makedirs(types + build_type + "/res" + dir)
            resize_square_image("ic_launcher.png", types + build_type + "/res" + dir + "ic_launcher.png", res)

    dir_res = [("/drawable-hdpi/", 216), ("/drawable-xhdpi/", 288), ("/drawable-mdpi/", 144),
               ("/drawable-xxhdpi/", 432)]
    for build_type in ['/release', '/debug']:
        for (dir, res) in dir_res:
            print "Writing ic_walkthrough.png with res %s to %s/%s" % (res, build_type, dir)
            if not os.path.exists(types + build_type + "/res" + dir): os.makedirs(types + build_type + "/res" + dir)
            resize_square_image("ic_launcher.png", types + build_type + "/res" + dir + "ic_walkthrough.png", res)


def write_color_theme():
    color_primary = colors.hex(get_value(config, "COLOR_PRIMARY"))
    color_primary_dark = colors.hex(get_value(config, "COLOR_PRIMARY_DARK"))
    color_accent = colors.hex(get_value(config, "COLOR_ACCENT"))
    color_primary_text = colors.hex(get_value(config, "COLOR_PRIMARY_TEXT"))
    color_secondary_text = colors.hex(get_value(config, "COLOR_SECONDARY_TEXT"))
    color_icon_on_primary = colors.hex(get_value(config, "COLOR_ICON_ON_PRIMARY"))
    color_divider = colors.hex(get_value(config, "COLOR_DIVIDER"))
    color_text_on_primary = colors.hex(get_value(config, "COLOR_TEXT_ON_PRIMARY"))

    for type in ['/release', '/debug']:
        f = open(types + type + src_package + "/colors.xml", "w")
        f.writelines("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        f.writelines("<resources>\n")
        f.writelines("<color name=\"%s\">%s</color>\n" % ("primary", color_primary.hex))
        f.writelines("<color name=\"%s\">%s</color>\n" % ("primary_dark", color_primary_dark.hex))
        f.writelines("<color name=\"%s\">%s</color>\n" % ("accent", color_accent.hex))
        f.writelines("<color name=\"%s\">#88%s</color>\n" % ("trans_accent", color_accent.hex[1:]))
        f.writelines("<color name=\"%s\">#D9%s</color>\n" % ("primary_text", color_primary_text.hex[1:]))
        f.writelines("<color name=\"%s\">#D9%s</color>\n" % ("secondary_text", color_secondary_text.hex[1:]))
        f.writelines("<color name=\"%s\">%s</color>\n" % ("icons_on_primary", color_icon_on_primary.hex))
        f.writelines("<color name=\"%s\">%s</color>\n" % ("divider", color_divider.hex))
        f.writelines("<color name=\"%s\">#D9%s</color>\n" % ("text_on_primary", color_text_on_primary.hex[1:]))
        f.writelines("</resources>\n")
        f.close()


def generate_gradle_properties():
    response = urllib2.urlopen("%s/api/3/app/build/android"
                               "?id=%s"
                               "&builder=true"
                               "&secret=LNbSx3LpNhGgHn3dVdhBY5q2" % (SERVER_URL, APP_ID))
    response_str = response.read()
    config_json = json.loads(response_str)["data"]
    template = open("gradle.properties_template").read()
    config_str = template % {"app_id": APP_ID,
                             "app_name": config_json.get("display_name", "Unknown"),
                             "package_name": config_json.get("package_name", "com.chatwing.unknown_package_name"),
                             "release_key_path": WHITE_LABEL_KEY_PATH,
                             "release_store_password": WHITE_LABEL_STORE_PASSWORD,
                             "release_key_password": WHITE_LABEL_KEY_PASSWORD,
                             "release_key_alias": WHITE_LABEL_KEY_ALIAS,
                             "facebook_app_id": config_json.get("facebook_app_id", ""),
                             "ic_launcher": ICON_LAUNCHER,

                             "color_primary": config_json["android_settings"].get("color_primary", "#9E9E9E"),
                             "color_primary_dark": config_json["android_settings"].get("color_primary_dark", "#212121"),
                             "color_accent": config_json["android_settings"].get("color_accent", "#FF5722"),
                             "color_primary_text": config_json["android_settings"].get("color_primary_text", "#212121"),
                             "color_secondary_text": config_json["android_settings"].get("color_secondary_text",
                                                                                         "#727272"),
                             "color_icon_on_primary": config_json["android_settings"].get("color_icon_on_primary",
                                                                                          "#FFFFFF"),
                             "color_divider": config_json["android_settings"].get("color_divider", "#B6B6B6"),
                             "color_text_on_primary": config_json["android_settings"].get("color_text_on_primary",
                                                                                          "#FFFFFF"),

                             "ads": "Yes" if config_json.get("admob_code", None) else "No",
                             "admob_ad_unit": config_json.get("admob_code", "") if config_json.get("admob_code",
                                                                                                   "") else "",
                             "login_method": LOGIN_METHOD,

                             "furry_api_key": FLURRY_API_KEY,

                             "allow_register": "Yes" if config_json['json'].get("openRegistration", True) else "No",
                             "version_code": VERSION_CODE,
                             "version_name": VERSION_NAME
                             }

    print config_str
    f = open("gradle.properties", "w")
    f.write(config_str)
    f.close()


# ensure_out_folder()
generate_gradle_properties()
config = load_config()

os.system("./gradlew clean")
ensure_build_type_folders()

write_string_xml()
write_bool_xml()
write_icons()
write_color_theme()

print(get_value(config, "APP_NAME"))
os.system("./gradlew assembleRelease")
