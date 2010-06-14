/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
*/
package com.servoy.extensions.plugins.xmlreader;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.servoy.j2db.scripting.IScriptObject;

/**
 * @author jcompagner
 */
@SuppressWarnings("nls")
public class XmlNode implements IScriptObject
{
	private final Node node;

	// default constructor just for the tree.
	public XmlNode()
	{
		node = null;
	}

	/**
	 * @param node
	 */
	public XmlNode(Node node)
	{
		this.node = node;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getSample(java.lang.String)
	 */
	public String getSample(String methodName)
	{
		if ("getType".equals(methodName) || "getName".equals(methodName) || "getTextValue".equals(methodName) || "getChildNodes".equals(methodName))
		{
			return "" + "nodes = plugins.XmlReader.readXmlDocumentFromString('<father><child1>John</child1><child2>Mary</child2></father>');" + "\n"
				+ "application.output(nodes[0].getName())" + "\n" + "application.output(nodes[0].getTextValue())" + "\n"
				+ "application.output(nodes[0].getType())" + "\n" + "childs = nodes[0].getChildNodes()" + "\n" + "application.output(childs[0].getName())"
				+ "\n" + "application.output(childs[0].getTextValue())" + "\n" + "application.output(childs[0].getType())" + "\n"
				+ "subChilds = childs[0].getChildNodes()" + "\n" + "application.output(subChilds[0].getName())" + "\n"
				+ "application.output(subChilds[0].getTextValue())" + "\n" + "application.output(subChilds[0].getType())";
		}
		else if ("getAttributeValue".equals(methodName) || "getAttributeNames".equals(methodName))
		{
			return "nodes = plugins.XmlReader.readXmlDocumentFromString(\"<root attr1='value1' attr2='value2'/>\")" + "\n" + "rootNode = nodes[0];" + "\n"
				+ "attributes = rootNode.getAttributeNames();" + "\n" + "application.output(attributes[0])" + "\n" + "application.output(attributes[1])" + "\n"
				+ "val1 = rootNode.getAttributeValue('attr1');" + "\n" + "application.output(val1)";
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(java.lang.String)
	 */
	public String getToolTip(String methodName)
	{
		if ("getType".equals(methodName))
		{
			return "Return the type of the XML node element.";
		}
		else if ("getName".equals(methodName))
		{
			return "Return the name of the XML node element.";
		}
		else if ("getTextValue".equals(methodName))
		{
			return "Return the text-value of the XML node element.";
		}
		else if ("getChildNodes".equals(methodName))
		{
			return "Return the child nodes of the current node.";
		}
		else if ("getAttributeValue".equals(methodName))
		{
			return "Return the value of the named attribute.";
		}
		else if ("getAttributeNames".equals(methodName))
		{
			return "Return all the attribute names of the current node.";
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getParameterNames(java.lang.String)
	 */
	public String[] getParameterNames(String methodName)
	{
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#isDeprecated(java.lang.String)
	 */
	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return null;
	}

	/**
	 * Return the name of the xml node element
	 *
	 * @sampleas js_getChildNodes()
	 */
	public String js_getName()
	{
		return node.getNodeName();
	}

	/**
	 * Return the type of the xml node element
	 *
	 * @sampleas js_getChildNodes()
	 */
	public String js_getType()
	{
		short type = node.getNodeType();
		switch (type)
		{
			case Node.TEXT_NODE :
			{
				return "TEXT";
			}
			case Node.ELEMENT_NODE :
			{
				NodeList nl = node.getChildNodes();
				if (nl != null && nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE)
				{
					return "TEXT";
				}
				return "ELEMENT";
			}
		}
		return "UNKNOWN";
	}

	/**
	 * Return the text-value of the xml node element
	 *
	 * @sampleas js_getChildNodes()
	 */
	public String js_getTextValue()
	{
		if (node.getNodeType() == Node.TEXT_NODE)
		{
			return node.getNodeValue();
		}
		NodeList nl = node.getChildNodes();
		if (nl != null && nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE)
		{
			return nl.item(0).getNodeValue();
		}
		return node.getNodeValue();
	}

	/**
	 * Return the value of the named attribute
	 *
	 * @sampleas js_getAttributeNames()
	 */
	public String js_getAttributeValue(String attributeName)
	{
		return node.getAttributes().getNamedItem(attributeName).getNodeValue();
	}

	/**
	 * Return all the attribute names of the
	 *
	 * @sample
	 * nodes = plugins.XmlReader.readXmlDocumentFromString("<root attr1='value1' attr2='value2'/>")
	 * rootNode = nodes[0];
	 * attributes = rootNode.getAttributeNames();
	 * application.output(attributes[0])
	 * application.output(attributes[1])
	 * val1 = rootNode.getAttributeValue('attr1');
	 * application.output(val1)
	 */
	public String[] js_getAttributeNames()
	{
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null)
		{
			int length = attributes.getLength();
			String[] names = new String[length];
			for (int i = 0; i < names.length; i++)
			{
				names[i] = attributes.item(i).getNodeName();
			}
			return names;
		}
		return new String[0];
	}

	/**
	 * Return the child nodes of the current node;
	 *
	 * @sample
	 * nodes = plugins.XmlReader.readXmlDocumentFromString('<father><child1>John</child1><child2>Mary</child2></father>');
	 * application.output(nodes[0].getName())
	 * application.output(nodes[0].getTextValue())
	 * application.output(nodes[0].getType())
	 * childs = nodes[0].getChildNodes()
	 * application.output(childs[0].getName())
	 * application.output(childs[0].getTextValue())
	 * application.output(childs[0].getType())
	 * subChilds = childs[0].getChildNodes()
	 * application.output(subChilds[0].getName())
	 * application.output(subChilds[0].getTextValue())
	 * application.output(subChilds[0].getType())
	 */
	public XmlNode[] js_getChildNodes()
	{
		NodeList nl = node.getChildNodes();
		List<XmlNode> al = new ArrayList<XmlNode>();
		for (int i = 0; i < nl.getLength(); i++)
		{
			Node item = nl.item(i);
			if (item.getNodeType() == Node.TEXT_NODE)
			{
				String value = item.getNodeValue();
				if (value == null || "".equals(value.trim()))
				{
					continue;
				}
			}
			al.add(new XmlNode(item));
		}
		XmlNode[] nodes = al.toArray(new XmlNode[al.size()]);
		return nodes;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Node[name:");
		sb.append(node.getNodeName());
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null && attributes.getLength() > 0)
		{
			sb.append(", attributes: {");
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node n = attributes.item(i);
				sb.append("name:");
				sb.append(n.getNodeName());
				sb.append(",value:");
				sb.append(n.getNodeValue());
				sb.append("|");
			}
			sb.setLength(sb.length() - 1);
			sb.append("}");
		}
		sb.append(", type: ");
		sb.append(js_getType());
		sb.append("]");
		return sb.toString();
	}
}