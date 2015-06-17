import ConfigParser
from functools import partial
from itertools import chain
import shutil
import os
import colors
import colorsys
import urllib2
import json
CONFIG_FILE = "gradle.properties"
types = 'app/build-types'
src_package = "/res/values"
STRING_MAPPING = {"APP_NAME": "app_name","FACEBOOK_APP_ID":"fb_app_id","LOGIN_METHOD":"build_login_method","FLURRY_API_KEY":"flurry_api_key"}
BOOL_MAPPING = {"OFFICIAL":"official","ADS":"show_ads","ALLOW_REGISTER":"allow_register","SUPPORT_RSS":"support_rss","SUPPORT_MUSIC_BOX":"support_music_box"}

APP_ID = "b4b391d0-e9bf-11e4-871f-f1829c245e2e"
OUT_APP_PATH = "out/%s"%APP_ID
SERVER_URL = "http://staging.chatwing.com"
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
    hsv2 = 0.94
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


def ensure_out_folder():
    if not os.path.exists("out"):
        os.makedirs("out")
    if not os.path.exists(OUT_APP_PATH):
        os.makedirs(OUT_APP_PATH)
def ensure_cert(config_json):
    if os.path.exists("%s/whitelabel.keystore"%OUT_APP_PATH): return

    cmd = """keytool -genkeypair -alias %(alias)s -keypass %(key_pass)s -keystore %(key_path)s -storepass %(key_pass)s -dname "CN=%(cn)s,O=%(o)s,C=%(c)s" -validity 9999""" \
          %{
                "alias":config_json.get("key_alias",""),
                "key_pass":config_json.get("key_password",""),
                "key_path":"%s/whitelabel.keystore"%OUT_APP_PATH,
                "cn":config_json.get("common_name",""),
                "o":config_json.get("organization",""),
                "c":config_json.get("country",""),
            }
    print cmd
    os.system(cmd)
def generate_gradle_properties():
    response = urllib2.urlopen("http://staging.chatwing.com/api/3/app/build/android"
                    "?id=%s"
                    "&secret=LNbSx3LpNhGgHn3dVdhBY5q2"%APP_ID)
    response_str = response.read()
    config_json = json.loads(response_str)["data"]
    ensure_cert(config_json)
    template = open("gradle.properties_template").read()
    config_str = template % {"app_name":config_json.get("display_name","Unknown"),
                             "package_name":config_json.get("package_name","com.chatwing.unknown_package_name"),
                             "release_key_path":"../%s/whitelabel.keystore"%OUT_APP_PATH,
                             "release_key_password":config_json.get("key_password",""),
                             "release_key_alias":config_json.get("key_alias",""),
                             "facebook_app_id":config_json.get("facebook_app_id",""),
                             "color_theme":config_json.get("color_theme","#05b0ff"),
                             "ic_launcher":"%s%s"%(SERVER_URL,config_json.get("icon","")),
                             "color_action_primary":config_json.get("color_action_primary","#E92754"),
                             "allow_register":"Yes" if config_json.get("openRegistration",True) else "No",
                             "login_method":config_json.get("loginMethod",True)
                             }

    print config_str
    f = open("gradle.properties","w")
    f.write(config_str)



#ensure_out_folder()
#generate_gradle_properties()

os.system("./gradlew clean")
ensure_build_type_folders()
write_string_xml()
write_bool_xml()
write_ic_launcher()
write_color_theme()
# #create_certs()
#
print(get_value(config, "APP_NAME"))
os.system("./gradlew assembleRelease")

