import httplib
import json
import xlrd
import ast
import math

r=100
s=100000000
t=100
H=0.8

class CCBalancer(object):

	def __init__(self, server):
		self.server = server


	def set(self, data):
		ret = self.rest_call(data, "POST")
		return ret[0] == 200



	def rest_call(self, data, action):
		path = "/wm/ccbalancer/topocosts/json"
		headers = {
			"Content-type": "topocosts/json",
			"Accept": "topocosts/json",
			}
		body = json.dumps(data)
		conn = httplib.HTTPConnection(self.server, 8080)
		conn.request(action, path, body, headers)
		response = conn.getresponse()
		ret = (response.status, response.reason, response.read())
		print ret
		conn.close()
		return ret
		
pusher = CCBalancer("127.0.0.1")
x = 0
stringa = "["
book = xlrd.open_workbook("example.xls")
sh = book.sheet_by_index(0)
col=0;
valoritotali = sh.nrows
while x < valoritotali:
	if x != 0:
		stringa = stringa + ","
	col=sh.row_len(x)
	src = str(sh.cell_value(rowx=x, colx=0))
	outPort = str(int(sh.cell_value(rowx=x, colx=1)))
	dst = str(sh.cell_value(rowx=x, colx=2))
	inPort = str(int(sh.cell_value(rowx=x, colx=3)))
	BT = 10000000
	BW = sh.cell_value(rowx=x, colx=col-1)*1000000
	BL = float(BT - BW)
	PBW = BW/BT
	
	if BW < H*BT:
		G = 0
	else:
		G = r*(((BW-(H*BT))/(BT-(H*BT))))
	cost = str(int(math.floor(((r*PBW)+1)*(s/BT)+((s/BT)*G))))
	print src, dst, cost
	stringa = stringa + "{'src':'" + src + "','outPort':'" + outPort + "','dst':'" + dst + "','inPort':'" + inPort + "','cost':'" + cost + "'}"
	x = x + 1
stringa = stringa + "]"
print stringa
#pusher.set(ast.literal_eval(stringa))