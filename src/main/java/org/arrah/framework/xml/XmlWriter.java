package org.arrah.framework.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The class used to write the row and column data of a JTable into an XML file
 * 
 * @author jchamblee
 */
public class XmlWriter {

	/**
	 * This method writes the JTable to an XML file.
	 * 
	 * @param jtable
	 *            a table of data
	 * @param fileName
	 *            filename
	 * @exception IOException
	 *                Description of the Exception
	 */
	public void writeXmlFile(final JTable jtable, final String fileName)
			throws IOException {
		final Document document = createDomDocument(jtable);
		assert document != null;
		writeDomDocument(document, fileName);
	}

	/**
	 * Create an XML document from a JTable
	 * 
	 * @param jtable
	 *            a table of data
	 * @return an XML document with the row and column data
	 */
	private Document createDomDocument(final JTable jtable) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		Document document = null;
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			final Element rootElement = document.createElement("table");
			rootElement.setAttribute("Name", "TableName");
			document.appendChild(rootElement);
			final Element columnNames = document.createElement("header");
			// Get column names
			for (int j = 0; j < jtable.getColumnCount(); j++) {
				final Element curElement = document.createElement("columnName");
				curElement.appendChild(document.createTextNode(jtable
						.getColumnName(j).trim()));
				columnNames.appendChild(curElement);
			}
			rootElement.appendChild(columnNames);
			// Get row data
			for (int i = 0; i < jtable.getRowCount(); i++) {
				final Element rowElement = document.createElement("row");
				for (int j = 0; j < jtable.getColumnCount(); j++) {
					try {
						final Object cellValue = jtable.getValueAt(i, j);
						final String curValue = cellValue == null ? ""
								: cellValue.toString();
						final Element curElement = document
								.createElement(jtable.getColumnName(j).trim()); // in case some whitespace
						curElement.appendChild(document.createTextNode(curValue));
						rowElement.appendChild(curElement);
					} catch (DOMException exc) {
						if (exc.code == DOMException.INVALID_CHARACTER_ERR) {
							System.out.println("\nInvalid data: "
									+ jtable.getColumnName(j) + " " + " "
									+ exc.getMessage());
						} else {
							System.out
									.println("Unexpected error code of exception: "
											+ exc);
						}
					} // End of code
				}
				rootElement.appendChild(rowElement);
			}
			document.getDocumentElement().normalize();

		} catch (ParserConfigurationException exc) {
			System.out.println("\n XmlWriter error:" + exc.getMessage());
		}
		return document;
	}

	/**
	 * This method writes the document object to an xml file.
	 * 
	 * @param document
	 *            an XML document
	 * @param xmlFilename
	 *            XML file name
	 * @exception IOException
	 *                Description of the Exception
	 */
	private void writeDomDocument(final Document document,
			final String xmlFilename) throws IOException {
		try {
			final TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DTDGenerator dtdGenerator = new DTDGenerator();
			final File xmlFile = new File(xmlFilename);
			final String dtdFilename = dtdGenerator.getDtdFilename(xmlFile);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "file:/"
					+ dtdFilename);
			final Source source = new DOMSource(document);
			final Result dest = new StreamResult(xmlFile.getAbsolutePath());
			transformer.transform(source, dest);
		} catch (TransformerConfigurationException exp) {
			System.err.println(exp.toString());
		} catch (TransformerException exp) {
			System.err.println(exp.toString());
		}
	}
	/**
	* Create an XML document from a JTable
	*
	* @param jtable a table of data
	* @return an XML document with the row and column data
	*/
	private Document parseDocument(final File file) {
		Document document = null;
	try {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		document = docBuilder.parse(file);
		document.getDocumentElement().normalize();
	} catch (SAXException exc) {
		System.out.println("\n XmlReader error:" + exc.getMessage());
	} catch (ParserConfigurationException exc) {
		System.out.println("\n XmlReader error:" + exc.getMessage());
	} catch (IOException exc) {
		System.out.println("\n XmlReader error:" + exc.getMessage());
	}
		return document;
	}
	
    public void writeXmlFile(Hashtable<String, String> hashRule) {
    	Path absolutePath = Paths.get("./configuration/");
    	File brFile = new File(absolutePath.toUri());

    	if (!brFile.exists()) {
    	brFile.mkdir();
    	}
    	brFile = new File(FilePaths.getFilePathRules());
    
        if (!brFile.exists()) {
            try {
                brFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (brFile.length() <= 0) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("businessrules");
                doc.appendChild(rootElement);

                Element rule = doc.createElement("rule");
                rootElement.appendChild(rule);

                Element ruleName = doc.createElement("rule_Name");
                ruleName.setTextContent(hashRule.get("rule_Name"));
                rule.appendChild(ruleName);

                Element database_ConnectionName = doc.createElement("database_ConnectionName");
                database_ConnectionName.setTextContent(hashRule.get("database_ConnectionName"));
                rule.appendChild(database_ConnectionName);

                Element rule_Type = doc.createElement("rule_Type");
                rule_Type.setTextContent(hashRule.get("rule_Type"));
                rule.appendChild(rule_Type);

                Element table_Names = doc.createElement("table_Names");
                table_Names.setTextContent(hashRule.get("table_Names"));
                rule.appendChild(table_Names);

                Element column_Names = doc.createElement("column_Names");
                column_Names.setTextContent(hashRule.get("column_Names"));
                rule.appendChild(column_Names);

                Element condition_Names = doc.createElement("condition_Names");
                condition_Names.setTextContent(hashRule.get("condition_Names"));
                rule.appendChild(condition_Names);
                
                Element join_Name = doc.createElement("join_Name");
                join_Name.setTextContent(hashRule.get("join_Name"));
                rule.appendChild(join_Name);
                
                Element rule_Desc = doc.createElement("rule_Description");
                rule_Desc.setTextContent(hashRule.get("rule_Description"));
                rule.appendChild(rule_Desc);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();

                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");

                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(brFile);

                transformer.transform(source, result);

                // System.out.println(hashRule.get("rule_Name") + "\n" + hashRule.get("database_ConnectionName") + "\n" + hashRule.get("rule_Type") + "\n" + hashRule.get("table_Names") + "\n" + hashRule.get("column_Names") + "\n" + hashRule.get("condition_Names"));

            } catch (ParserConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = documentBuilder.parse(brFile);
                Element root = doc.getDocumentElement();

                Element rule = doc.createElement("rule");

                Element ruleName = doc.createElement("rule_Name");
                ruleName.setTextContent(hashRule.get("rule_Name"));
                rule.appendChild(ruleName);

                Element database_ConnectionName = doc.createElement("database_ConnectionName");
                database_ConnectionName.setTextContent(hashRule.get("database_ConnectionName"));
                rule.appendChild(database_ConnectionName);

                Element rule_Type = doc.createElement("rule_Type");
                rule_Type.setTextContent(hashRule.get("rule_Type"));
                rule.appendChild(rule_Type);

                Element table_Names = doc.createElement("table_Names");
                table_Names.setTextContent(hashRule.get("table_Names"));
                rule.appendChild(table_Names);

                Element column_Names = doc.createElement("column_Names");
                column_Names.setTextContent(hashRule.get("column_Names"));
                rule.appendChild(column_Names);

                Element condition_Names = doc.createElement("condition_Names");
                condition_Names.setTextContent(hashRule.get("condition_Names"));
                rule.appendChild(condition_Names);
                
                Element join_Name = doc.createElement("join_Name");
                join_Name.setTextContent(hashRule.get("join_Name"));
                rule.appendChild(join_Name);

                Element rule_Desc = doc.createElement("rule_Description");
                rule_Desc.setTextContent(hashRule.get("rule_Description"));
                rule.appendChild(rule_Desc);
                
                root.appendChild(rule);
                doc.normalize();
                DOMSource source = new DOMSource(doc);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                StreamResult result = new StreamResult(brFile);
                transformer.transform(source, result);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void deleteNode(String nodeName) {
    	File brFile = new File(FilePaths.getFilePathRules());
        if (brFile.exists() && brFile.length() > 0) {
        	Document document = null;
            try {
                document = parseDocument(brFile);

                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer tFormer = tFactory.newTransformer();

                Element rootElement = document.getDocumentElement();
                NodeList nList = document.getElementsByTagName("rule");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    Element eElement = (Element) nNode;
                    if (eElement.getElementsByTagName("rule_Name").item(0).getTextContent().trim().equals(nodeName)) {
                        rootElement.removeChild(nNode);
                       // System.out.println(nodeName + "\nNode Name: " + eElement.getElementsByTagName("rule_Name").item(0).getTextContent().trim());
                    }
                }

                document.normalize();
                Source source = new DOMSource(document);
                Result result = new StreamResult(brFile);
                tFormer.transform(source, result);

            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void modifyRule( Hashtable<String, String> hashRule ) {
        try {
            Document document = parseDocument(new File( FilePaths.getRuleFilePath() ));
            
            NodeList rList = document.getElementsByTagName("rule");
            
            for (int i = 0; i < rList.getLength(); i++) {
                Node nNode = rList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if (eElement.getElementsByTagName("rule_Name").item(0).getTextContent().trim().equals(String.valueOf(hashRule.get("rule_Name")))) {
                        // System.out.println(eElement.getAttributes().getLength());
                        eElement.getElementsByTagName("rule_Name").item(0).setTextContent(hashRule.get("rule_Name"));
                        eElement.getElementsByTagName("database_ConnectionName").item(0).setTextContent(hashRule.get("database_ConnectionName"));
                        eElement.getElementsByTagName("rule_Type").item(0).setTextContent(hashRule.get("rule_Type"));
                        eElement.getElementsByTagName("table_Names").item(0).setTextContent(hashRule.get("table_Names"));
                        eElement.getElementsByTagName("column_Names").item(0).setTextContent(hashRule.get("column_Names"));
                        eElement.getElementsByTagName("condition_Names").item(0).setTextContent(hashRule.get("condition_Names"));
                        eElement.getElementsByTagName("join_Name").item(0).setTextContent(hashRule.get("join_Name"));
                        eElement.getElementsByTagName("rule_Description").item(0).setTextContent(hashRule.get("rule_Description"));
                    }
                }
            }
            document.normalize();
            Source source = new DOMSource(document);
            Result result = new StreamResult(FilePaths.getRuleFilePath());
            Transformer tFormer = TransformerFactory.newInstance().newTransformer();
            tFormer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    // Writeback the connection
    public void writeConnection(Hashtable<String, String> dbConnection) {
    	
    	Path absolutePath = Paths.get("./configuration/");
    	File brFile = new File(absolutePath.toUri());

    	if (!brFile.exists()) {
    	brFile.mkdir();
    	}
    	brFile = new File(FilePaths.getFilePathDB());

        if (!brFile.exists()) {
            try {
                brFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (brFile.length() <= 0) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("configuration");
                doc.appendChild(rootElement);

                Element connection = doc.createElement("connection");
                rootElement.appendChild(connection);

                Element entry = doc.createElement("entry");
                connection.appendChild(entry);

                Element value = doc.createElement("value");
                value.setAttribute("databaseCatalog", "");
                value.setAttribute("databaseColumnPattern", dbConnection.get("Database_ColumnPattern"));
                value.setAttribute("databaseConnectionName", dbConnection.get("Database_ConnectionName"));
                value.setAttribute("databaseDSN", dbConnection.get("Database_DSN"));
                value.setAttribute("databaseDriver", dbConnection.get("Database_Driver"));
                value.setAttribute("databaseJDBC", dbConnection.get("Database_JDBC"));
                value.setAttribute("databasePasswd", dbConnection.get("Database_Passwd"));
                value.setAttribute("databaseProtocol", dbConnection.get("Database_Protocol"));
                value.setAttribute("databaseSchemaPattern", dbConnection.get("Database_SchemaPattern"));
                value.setAttribute("databaseTablePattern", dbConnection.get("Database_TablePattern"));
                value.setAttribute("databaseTableType", dbConnection.get("Database_TableType"));
                value.setAttribute("databaseType", dbConnection.get("Database_Type"));
                value.setAttribute("databaseUser", dbConnection.get("Database_User"));
                entry.appendChild(value);

                Element db = doc.createElement("database_ConnectionName");
                db.setTextContent(dbConnection.get("Database_ConnectionName"));
                entry.appendChild(db);

                db = doc.createElement("database_ColumnPattern");
                db.setTextContent(dbConnection.get("Database_ColumnPattern"));
                entry.appendChild(db);

                db = doc.createElement("database_DSN");
                db.setTextContent(dbConnection.get("Database_DSN"));
                entry.appendChild(db);

                db = doc.createElement("database_Driver");
                db.setTextContent(dbConnection.get("Database_Driver"));
                entry.appendChild(db);

                db = doc.createElement("database_JDBC");
                db.setTextContent(dbConnection.get("Database_JDBC"));
                entry.appendChild(db);

                db = doc.createElement("database_Passwd");
                db.setTextContent(dbConnection.get("Database_Passwd"));
                entry.appendChild(db);

                db = doc.createElement("database_Protocol");
                db.setTextContent(dbConnection.get("Database_Protocol"));
                entry.appendChild(db);

                db = doc.createElement("database_SchemaPattern");
                db.setTextContent(dbConnection.get("Database_SchemaPattern"));
                entry.appendChild(db);

                db = doc.createElement("database_TablePattern");
                db.setTextContent(dbConnection.get("Database_TablePattern"));
                entry.appendChild(db);

                db = doc.createElement("database_TableType");
                db.setTextContent(dbConnection.get("Database_TableType"));
                entry.appendChild(db);

                db = doc.createElement("database_Type");
                db.setTextContent(dbConnection.get("Database_Type"));
                entry.appendChild(db);

                db = doc.createElement("database_User");
                db.setTextContent(dbConnection.get("Database_User"));
                entry.appendChild(db);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();

                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");

                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(brFile);
                transformer.transform(source, result);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = documentBuilder.parse(brFile);
                Element root = doc.getDocumentElement();

                Element connection = doc.createElement("connection");

                Element entry = doc.createElement("entry");
                connection.appendChild(entry);

                Element value = doc.createElement("value");
                value.setAttribute("databaseCatalog", "");
                value.setAttribute("databaseColumnPattern", dbConnection.get("Database_ColumnPattern"));
                value.setAttribute("databaseConnectionName", dbConnection.get("Database_ConnectionName"));
                value.setAttribute("databaseDSN", dbConnection.get("Database_DSN"));
                value.setAttribute("databaseDriver", dbConnection.get("Database_Driver"));
                value.setAttribute("databaseJDBC", dbConnection.get("Database_JDBC"));
                value.setAttribute("databasePasswd", dbConnection.get("Database_Passwd"));
                value.setAttribute("databaseProtocol", dbConnection.get("Database_Protocol"));
                value.setAttribute("databaseSchemaPattern", dbConnection.get("Database_SchemaPattern"));
                value.setAttribute("databaseTablePattern", dbConnection.get("Database_TablePattern"));
                value.setAttribute("databaseTableType", dbConnection.get("Database_TableType"));
                value.setAttribute("databaseType", dbConnection.get("Database_Type"));
                value.setAttribute("databaseUser", dbConnection.get("Database_User"));
                entry.appendChild(value);

                Element db = doc.createElement("database_ConnectionName");
                db.setTextContent(dbConnection.get("Database_ConnectionName"));
                entry.appendChild(db);

                db = doc.createElement("database_ColumnPattern");
                db.setTextContent(dbConnection.get("Database_ColumnPattern"));
                entry.appendChild(db);

                db = doc.createElement("database_DSN");
                db.setTextContent(dbConnection.get("Database_DSN"));
                entry.appendChild(db);

                db = doc.createElement("database_Driver");
                db.setTextContent(dbConnection.get("Database_Driver"));
                entry.appendChild(db);

                db = doc.createElement("database_JDBC");
                db.setTextContent(dbConnection.get("Database_JDBC"));
                entry.appendChild(db);

                db = doc.createElement("database_Passwd");
                db.setTextContent(dbConnection.get("Database_Passwd"));
                entry.appendChild(db);

                db = doc.createElement("database_Protocol");
                db.setTextContent(dbConnection.get("Database_Protocol"));
                entry.appendChild(db);

                db = doc.createElement("database_SchemaPattern");
                db.setTextContent(dbConnection.get("Database_SchemaPattern"));
                entry.appendChild(db);

                db = doc.createElement("database_TablePattern");
                db.setTextContent(dbConnection.get("Database_TablePattern"));
                entry.appendChild(db);

                db = doc.createElement("database_TableType");
                db.setTextContent(dbConnection.get("Database_TableType"));
                entry.appendChild(db);

                db = doc.createElement("database_Type");
                db.setTextContent(dbConnection.get("Database_Type"));
                entry.appendChild(db);

                db = doc.createElement("database_User");
                db.setTextContent(dbConnection.get("Database_User"));
                entry.appendChild(db);

                root.appendChild(connection);
                doc.normalize();
                DOMSource source = new DOMSource(doc);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                StreamResult result = new StreamResult(brFile);
                transformer.transform(source, result);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(XmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


	
} // End of XMLWriter Class
