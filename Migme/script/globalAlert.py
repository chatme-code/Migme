#!/usr/bin/python
import os
import time
import optparse
import exceptions

class Adapter(object):
    def __init__(self, host, port):
        self.host = host
        self.port = port
    def get_locator(self):
        return "GatewayAdmin:tcp -h %s -p %d" % (self.host, self.port)
    def set_adapter(self, adapter):
        self.adapter = adapter
    def getStats(self):
        return self.adapter.getStats()
    def sendAlertToAllConnections(self, message, title):
        self.adapter.sendAlertToAllConnections(message, title)
    def pretty(self):
        return "host:%s port:%d" % (self.host, self.port)

def configure_ice():
    import Ice
    if not os.path.isfile("Fusion.ice"):
        raise IOError("unable to load slice file")
    Ice.loadSlice("Fusion.ice")
    ic = Ice.initialize()
    return ic

def get_gateway_locations(env):
    if env == "production":
        servers = ["api.migxchat.net" % k for k in range(1,16)]
    	return [Adapter(host=host, port=port) for port in [9998, 19998, 29998, 39998, 49998, 59998] for host in servers]
    elif env == "staging":
        return [Adapter(host="api.migxchat.net", port=port) for port in [9994,9998,19998,29998]]
    elif env == "miab":
        return [Adapter(host="localhost", port=port) for port in [9973,9977,9994]]

def create_gateway_adapters(adapters):
    _adapters = []
    ic = configure_ice()
    import com.projectgoth.fusion.slice as fusionslice
    for a in adapters:
        try:
            base = ic.stringToProxy(a.get_locator())
            prx = fusionslice.GatewayAdminPrx.checkedCast(base)
            a.set_adapter(prx)
            _adapters.append(a)
        except Exception, e:
            print "Exception: %s" % e
            pass

    return _adapters


if __name__ == "__main__":
    parser = optparse.OptionParser()
    parser.add_option("","--target-environment", dest="target_environment", default="miab", help="target environment, one of production, staging or miab")
    opts, args = parser.parse_args()
	
    if len(args) < 1:
        print "Usage: globalAlert \"message\" \"title\""
    else:
        message = args[0]
        title = "System" if len(args) < 2 else args[1]

        print "Sending message: \"%s\", title: \"%s\"" % (message, title)

        gateway_addresses = get_gateway_locations( opts.target_environment )
        adapters = create_gateway_adapters(gateway_addresses)
        for adapter in adapters:
            adapter.sendAlertToAllConnections(message, title)
