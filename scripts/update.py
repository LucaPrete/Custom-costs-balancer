########################################################
#
# Copyright (C) 2013 Luca Prete, Simone Visconti, Andrea Biancini, Fabio Farina - www.garr.it - Consortium GARR
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# author Luca Prete <luca.prete@garr.it>
# author Andrea Biancini <andrea.biancini@garr.it>
# author Fabio Farina <fabio.farina@garr.it>
# author Simone Visconti<simone.visconti.89@gmail.com>
#
########################################################

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
row = "["
book = xlrd.open_workbook("example.xls")
sh = book.sheet_by_index(0)
col=0;
numval = sh.nrows
while x < numval:
	if x != 0:
		row = row + ","
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
	row = row + "{'src':'" + src + "','outPort':'" + outPort + "','dst':'" + dst + "','inPort':'" + inPort + "','cost':'" + cost + "'}"
	x = x + 1
row = row + "]"
pusher.set(ast.literal_eval(row))