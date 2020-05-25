#!/usr/bin/python3

from anytree import Node, RenderTree
from anytree.exporter import DotExporter, UniqueDotExporter

# necessary to import local modules
import os
import sys
sys.path.insert(0, os.path.abspath('.'))
from get_pom_metadata import version_path

filename = "_static/ohsome-api-tree"


def add_groupBy_no_key(cur_root):
    bound = Node("groupBy/boundary", parent=cur_root)
    bound_tag = Node("groupBy/tag", parent=bound)
    tag = Node("groupBy/tag", parent=cur_root)
    _type = Node("groupBy/type", parent=cur_root)


def add_groupBy_full(cur_root):
    key = Node("groupBy/key", parent=cur_root)
    add_groupBy_no_key(cur_root)

def node_attributes(node):
    default_attributes = 'label="%s"' % (node.name, )
    path = []
    for n in node.path:
        path.append(n.name.split(',')[0] if "," in n.name else n.name)
    url = node.separator.join(path)
    return '{}\nURL="{}"'.format(default_attributes, url)


root = Node("https://api.ohsome.org")
vers = Node(version_path(), parent=root)
elem = Node("elements", parent=vers)
nodes = []
#for e in ["area", "count", "length", "perimeter"]:
for e in ["area, count, length, perimeter"]:
    cur_root = Node(e, parent=elem)
    dens = Node("density", parent=cur_root)
    add_groupBy_full(cur_root)
    add_groupBy_no_key(dens)
    rati = Node("ratio", parent=cur_root)
    Node("groupBy/boundary", parent=rati)

user = Node("users", parent=vers)
coun = Node("count", parent=user)
dens = Node("density", parent=coun)
Node("groupBy/boundary", parent=dens)
Node("groupBy/tag", parent=dens)
Node("groupBy/type", parent=dens)
Node("groupBy/boundary", parent=coun)
Node("groupBy/key", parent=coun)
Node("groupBy/tag", parent=coun)
Node("groupBy/type", parent=coun)

elemF = Node("elementsFullHistory", vers)
for e in [elem, elemF]:
    Node("bbox", parent=e)
    Node("centroid", parent=e)
    Node("geometry", parent=e)

Node("metadata", vers)

text_out = ""
for pre, fill, node in RenderTree(root):
    text_out += "%s%s\n" % (pre, node.name)

with open(filename + ".txt", "w") as fw:
    fw.write(text_out)

DotExporter(root, nodeattrfunc=node_attributes).to_picture(filename + ".svg")
# currently unused
#UniqueDotExporter(root, nodeattrfunc=node_attributes).to_picture(filename + ".unique.svg")
