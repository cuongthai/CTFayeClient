import ConfigParser
from functools import partial
from itertools import chain
import shutil
import os
import colors
import colorsys
CONFIG_FILE = "gradle.properties"
types = 'app/build-types'
src_package = "/res/values"
STRING_MAPPING = {"APP_NAME": "app_name","FACEBOOK_APP_ID":"fb_app_id"}
BOOL_MAPPING = {"OFFICIAL":"official","ADS":"show_ads"}

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


config = load_config()


def ensure_build_type_folders():
    if os.path.exists(types):
        shutil.rmtree(types)

    os.mkdir(types)
    os.makedirs(types + "/release" + src_package)
    os.makedirs(types + "/debug" + src_package)


def write_string_xml():
    for type in ['/release','/debug']:
        f = open(types + type + src_package + "/strings.xml", "w")
        f.writelines("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        f.writelines("<resources>\n")
        for appName in STRING_MAPPING:
            f.writelines("<string name=\"%s\">%s</string>\n" % (STRING_MAPPING[appName], get_value(config, appName)))
        f.writelines("</resources>\n")
        f.close()
def write_bool_xml():
    for type in ['/release','/debug']:
        f = open(types + type + src_package + "/bools.xml", "w")
        f.writelines("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        f.writelines("<resources>\n")
        for key in BOOL_MAPPING:
            val = get_value(config, key)
            f.writelines("<bool name=\"%s\">%s</bool>\n" % (BOOL_MAPPING[key], "true" if "Yes"==val else "false"))
        f.writelines("</resources>\n")
        f.close()

def write_ic_launcher():
    import urllib2
    url= get_value(config,"IC_LAUNCHER")
    print url
    icon = urllib2.urlopen(url)
    output = open('ic_launcher.png','wb')
    output.write(icon.read())
    output.close()

    for type in ['/release','/debug']:
        for dir in ["/drawable-hdpi/","/drawable-xhdpi/","/drawable-mdpi/","/drawable-xxhdpi/"]:
            os.makedirs(types + type +"/res"+dir)
            shutil.copyfile('ic_launcher.png',types + type +"/res"+dir+"ic_launcher.png")
def getTextColorHex(color):
    a = 1 - ( 0.299 * color.r*255.0 + 0.587 * color.g*255.0 + 0.114 * color.b*255.0)/255.0;
    print a
    if a<0.5:
        return colors.hex("#000000")
    else:
        return colors.hex("#FFFFFF")

def getLighterShadeColor(color):
    hls = colorsys.rgb_to_hls(color.r, color.g, color.b)
    print hls[1]
    hsv2 = 0.97
    rgb = colorsys.hls_to_rgb(hls[0], hsv2, hls[2])
    return colors.rgb(rgb[0],rgb[1],rgb[2])

def write_color_theme():
    theme_main_color = colors.hex(get_value(config,"COLOR_THEME"))
    action_primary_color = colors.hex(get_value(config,"COLOR_ACTION_PRIMARY"))
    action_secondary_color = colors.hex(get_value(config,"COLOR_ACTION_SECONDARY"))
    textColor= colors.hex(get_value(config,"COLOR_TEXT"))
    textActionColor= colors.hex(get_value(config,"COLOR_ACTION_TEXT"))
    bgColor = getLighterShadeColor(theme_main_color)
    for type in ['/release','/debug']:
        f = open(types + type + src_package + "/colors.xml", "w")
        f.writelines("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        f.writelines("<resources>\n")
        f.writelines("<color name=\"%s\">%s</color>\n" % ("primary_color",theme_main_color.hex))
        f.writelines("<color name=\"%s\">%s</color>\n" % ("action_primary_color",action_primary_color.hex))
        f.writelines("<color name=\"%s\">%s</color>\n" % ("action_secondary_color",action_secondary_color.hex))
        f.writelines("<color name=\"%s\">#D9%s</color>\n" % ("text_color",textColor.hex[1:]))
        f.writelines("<color name=\"%s\">#D9%s</color>\n" % ("text_action_color",textActionColor.hex[1:]))
        f.writelines("<color name=\"%s\">%s</color>\n" % ("bg_color",bgColor.hex))
        f.writelines("</resources>\n")
        f.close()

def create_certs():
    if os.path.exists("certs"):
        shutil.rmtree("certs")
    os.mkdir("certs")
    alias = get_value(config,"RELEASE_KEY_ALIAS")
    release_key = get_value(config,"RELEASE_KEY_PASSWORD")
    release_store = get_value(config,"RELEASE_STORE_PASSWORD")
    cn = get_value(config,"DNAME_CN")
    o = get_value(config,"DNAME_O")
    c = get_value(config,"D_NAME_C")
    cmd = """keytool -genkeypair -alias %s -keypass %s -keystore certs/whitelabel.keystore -storepass %s -dname "CN=%s,O=%s,C=%s" -validity 9999""" %(alias,release_key,release_store,cn,o,c)
    print cmd
    os.system(cmd)

os.system("./gradlew clean")
ensure_build_type_folders()
write_string_xml()
write_bool_xml()
write_ic_launcher()
write_color_theme()
#create_certs()

print(get_value(config, "APP_NAME"))
os.system("./gradlew assembleRelease")

