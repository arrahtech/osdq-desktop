package org.arrah.framework.xml;

import com.icl.saxon.*;
import com.icl.saxon.om.Axis;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.pattern.NodeTypeTest;
import com.icl.saxon.sort.BinaryTree;
import com.icl.saxon.tinytree.TinyBuilder;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * DTDGenerator<BR>
 * Generates a possible DTD from an XML document instance. Uses SAXON to process
 * the document contents.
 * 
 * @author M.H.Kay
 */

public class DTDGenerator extends Controller {
	// alphabetical list of element types appearing in the document;
	// each has the element name as a key and an ElementDetails object
	// as the value

	/**
	 * Entry point Usage: java DTDGenerator input-file >output-file
	 * 
	 * @param args
	 *            Description of the Parameter
	 * @exception Exception
	 *                Description of the Exception
	 */

	public static void main(final String args[]) throws Exception {
		// Check the command-line arguments.
		if (args.length != 1) {
			System.err
					.println("Usage: java DTDGenerator input-file >output-file");
			System.exit(1);
		}

		// Instantiate and run the application
		final DTDGenerator app = new DTDGenerator();
		final DocumentInfo doc = app.prepare(args[0]);

		app.run(doc);
		app.save(System.out);
	}

	/**
	 * Constructor for the DTDGenerator object
	 */
	public DTDGenerator() {
		elementList = new BinaryTree();
	}

	/**
	 * Description of the Method
	 * 
	 * @param xmlFile
	 *            Description of the Parameter
	 * @param dtdFile
	 *            Description of the Parameter
	 * @exception TransformerException
	 *                Description of the Exception
	 * @exception FileNotFoundException
	 *                Description of the Exception
	 */
	public void save(final File xmlFile, final File dtdFile)
			throws FileNotFoundException, TransformerException {
		final PrintStream dtdPrintStream = new PrintStream(
				new FileOutputStream(dtdFile.getAbsolutePath()));
		final DocumentInfo documentInfo = prepare(xmlFile.getAbsolutePath());
		run(documentInfo);
		save(dtdPrintStream);
	}

	/**
	 * Escape special characters for display.
	 * 
	 * @param character
	 *            The character array containing the string
	 * @param start
	 *            The start position of the input string within the character
	 *            array
	 * @param length
	 *            The length of the input string within the character array
	 * @param out
	 *            Description of the Parameter
	 * @return The XML/HTML representation of the string<br>
	 *         This static method converts a Unicode string to a string
	 *         containing only ASCII characters, in which non-ASCII characters
	 *         are represented by the usual XML/HTML escape conventions (for
	 *         example, "&lt;" becomes "&amp;lt;"). Note: if the input consists
	 *         solely of ASCII or Latin-1 characters, the output will be equally
	 *         valid in XML and HTML. Otherwise it will be valid only in XML.
	 *         The escaped characters are written to the dest array starting at
	 *         position 0; the number of positions used is returned as the
	 *         result
	 */

	private static int escape(final char character[], final int start,
			final int length, final char[] out) {
		int ret = 0;
		for (int i = start; i < start + length; i++) {
			if (character[i] == '<') {
				("&lt;").getChars(0, 4, out, ret);
				ret += 4;
			} else if (character[i] == '>') {
				("&gt;").getChars(0, 4, out, ret);
				ret += 4;
			} else if (character[i] == '&') {
				("&amp;").getChars(0, 5, out, ret);
				ret += 5;
			} else if (character[i] == '\"') {
				("&#34;").getChars(0, 5, out, ret);
				ret += 5;
			} else if (character[i] == '\'') {
				("&#39;").getChars(0, 5, out, ret);
				ret += 5;
			} else if (character[i] <= 0x7f) {
				out[ret++] = character[i];
			} else {
				final String dec = "&#" + Integer.toString((int) character[i])
						+ ';';
				dec.getChars(0, dec.length(), out, ret);
				ret += dec.length();
			}
		}
		return ret;
	}

	/**
	 * Escape special characters in a String value.
	 * 
	 * @param input
	 *            The input string
	 * @return The XML representation of the string<br>
	 *         This static method converts a Unicode string to a string
	 *         containing only ASCII characters, in which non-ASCII characters
	 *         are represented by the usual XML/HTML escape conventions (for
	 *         example, "&lt;" becomes "&amp;lt;").<br>
	 *         Note: if the input consists solely of ASCII or Latin-1
	 *         characters, the output will be equally valid in XML and HTML.
	 *         Otherwise it will be valid only in XML.
	 */

	private static String escape(final String input) {
		final char[] dest = new char[input.length() * 8];
		final int newlen = escape(input.toCharArray(), 0, input.length(), dest);
		return new String(dest, 0, newlen);
	}

	/**
	 * Set up
	 * 
	 * @param filename
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception TransformerException
	 *                Description of the Exception
	 */

	private DocumentInfo prepare(final String filename)
			throws TransformerException {
		// Set the element handler for all elements

		final RuleManager ruleManager = new RuleManager(getNamePool());
		setRuleManager(ruleManager);
		ruleManager.setHandler("*", new ElemHandler());
		ruleManager.setHandler("text()", new CharHandler());

		// build the document

		final TinyBuilder builder = new TinyBuilder();
		builder.setNamePool(getNamePool());
		final InputSource inputSource = new ExtendedInputSource(new File(
				filename));
		final SAXSource saxSource = new SAXSource(inputSource);
		return builder.build(saxSource);
	}

	/**
	 * When the whole document has been analysed, construct the DTD
	 * 
	 * @param printStream
	 *            output stream
	 */

	private void save(final PrintStream printStream) {
		// process the element types encountered, in turn

		final Enumeration enu = elementList.getKeys().elements();
		while (enu.hasMoreElements()) {
			final String elementname = (String) enu.nextElement();
			final ElementDetails elementDetails = (ElementDetails) elementList
					.get(elementname);
			final BinaryTree children = elementDetails.children;
			final Vector childKeys = children.getKeys();

			// EMPTY content
			if (childKeys.isEmpty() && !elementDetails.hasCharacterContent) {
				printStream.print("<!ELEMENT " + elementname + " EMPTY >\n");
			}

			// CHARACTER content
			if (childKeys.isEmpty() && elementDetails.hasCharacterContent) {
				printStream.print("<!ELEMENT " + elementname
						+ " ( #PCDATA ) >\n");
			}

			// ELEMENT content
			if (!childKeys.isEmpty() && !elementDetails.hasCharacterContent) {
				printStream.print("<!ELEMENT " + elementname + " ( ");

				if (elementDetails.sequenced) {

					// all elements of this type have the same child elements
					// in the same sequence, retained in the childseq vector

					final Enumeration enumer = elementDetails.childseq
							.elements();
					while (true) {
						final ChildDetails childDetails = (ChildDetails) enumer
								.nextElement();
						printStream.print(childDetails.name);
						if (childDetails.repeatable && !childDetails.optional) {
							printStream.print("+");
						}
						if (childDetails.repeatable && childDetails.optional) {
							printStream.print("*");
						}
						if (childDetails.optional && !childDetails.repeatable) {
							printStream.print("?");
						}
						if (enumer.hasMoreElements()) {
							printStream.print(", ");
						} else {
							break;
						}
					}
					printStream.print(" ) >\n");
				} else {

					// the children don't always appear in the same sequence; so
					// list them alphabetically and allow them to be in any
					// order

					for (int c1 = 0; c1 < childKeys.size(); c1++) {
						if (c1 > 0) {
							printStream.print(" | ");
						}
						printStream.print((String) childKeys.elementAt(c1));
					}
					printStream.print(" )* >\n");
				}
			}

			// MIXED content
			if (!childKeys.isEmpty() && elementDetails.hasCharacterContent) {
				printStream.print("<!ELEMENT " + elementname + " ( #PCDATA");
				for (int c2 = 0; c2 < childKeys.size(); c2++) {
					printStream.print(" | " + (String) childKeys.elementAt(c2));
				}
				printStream.print(" )* >\n");
			}

			// Now examine the attributes encountered for this element type

			final BinaryTree attlist = elementDetails.attributes;
			boolean doneID = false;
			// to ensure we have at most one ID attribute per element
			final Enumeration enumeration = attlist.getKeys().elements();
			while (enumeration.hasMoreElements()) {
				final String attname = (String) enumeration.nextElement();
				final AttributeDetails attributeDetails = (AttributeDetails) attlist
						.get(attname);

				// if the attribute is present on every instance of the element,
				// treat it as required
				final boolean required = (attributeDetails.occurrences == elementDetails.occurrences);

				// if every value of the attribute is distinct, and there are
				// >10, treat it as an ID
				// (!!this may give the wrong answer, we should really check
				// whether the value sets of two
				// candidate-ID attributes overlap, in which case they can't
				// both be IDs !!)
				final boolean isid = attributeDetails.allNames
						&&
						// ID values must be Names
						(!doneID)
						&&
						// Only allowed one ID attribute per element type
						(attributeDetails.values.size() == attributeDetails.occurrences)
						&& (attributeDetails.occurrences > 10);

				// if there is only one attribute value, and 4 or more
				// occurrences of it, treat it as FIXED
				final boolean isfixed = required
						&& attributeDetails.values.size() == 1
						&& attributeDetails.occurrences > 4;

				// if the number of distinct values is small compared with the
				// number of occurrences,
				// treat it as an enumeration
				final boolean isenum = attributeDetails.allNMTOKENs
						&&
						// Enumeration values must be NMTOKENs
						(attributeDetails.occurrences > 10)
						&& (attributeDetails.values.size() <= attributeDetails.occurrences / 3)
						&& (attributeDetails.values.size() < 10);

				printStream.print("<!ATTLIST " + elementname + " " + attname
						+ " ");
				final String tokentype = (attributeDetails.allNMTOKENs ? "NMTOKEN"
						: "CDATA");

				if (isid) {
					printStream.print("ID");
					doneID = true;
				} else if (isfixed) {
					final String val = (String) attributeDetails.values
							.getKeys().elementAt(0);
					printStream.print(tokentype + " #FIXED \"" + escape(val)
							+ "\" >\n");
				} else if (isenum) {
					printStream.print("( ");
					final Vector vector = attributeDetails.values.getKeys();
					for (int v1 = 0; v1 < vector.size(); v1++) {
						if (v1 != 0) {
							printStream.print(" | ");
						}
						printStream.print((String) vector.elementAt(v1));
					}
					printStream.print(" )");
				} else {
					printStream.print(tokentype);
				}

				if (!isfixed) {
					if (required) {
						printStream.print(" #REQUIRED >\n");
					} else {
						printStream.print(" #IMPLIED >\n");
					}
				}
			}
			printStream.print("\n");
		}
	}

	/**
	 * Gets the DTD filename based on the XML filename. Replaces the extension
	 * ".xml" with ".dtd"
	 * 
	 * @param xmlFile
	 *            XML file
	 * @return The dtdFilename value
	 */
	public String getDtdFilename(final File xmlFile) {
		final String xmlFilename = xmlFile.getAbsolutePath();
		final int position = xmlFilename.lastIndexOf('.');
		if (position == -1) {
			return xmlFilename + ".dtd";
		} else {
			return xmlFilename.substring(0, position) + ".dtd";
		}
	}

	/**
	 * Determine whether an element is the first is a group of consecutive
	 * elements with the same name
	 * 
	 * @param node
	 *            Description of the Parameter
	 * @return The firstInGroup value
	 */

	private boolean isFirstInGroup(final NodeInfo node) {
		final AxisEnumeration prev = node.getEnumeration(
				Axis.PRECEDING_SIBLING, new NodeTypeTest(NodeInfo.ELEMENT));
		if (prev.hasMoreElements()) {
			final NodeInfo prevNode = prev.nextElement();
			return (!prevNode.getDisplayName().equals(node.getDisplayName()));
		} else {
			return true;
		}
	}

	/**
	 * Test whether a string is an XML NMTOKEN. This is currently an incomplete
	 * test, in that it treats all non-ASCII characters as being valid in
	 * NMTOKENs.
	 * 
	 * @param str
	 *            Description of the Parameter
	 * @return The validNMTOKEN value
	 */

	private boolean isValidNMTOKEN(final String str) {
		if (str.length() == 0) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			final int character = str.charAt(i);
			if (!((character >= 0x41 && character <= 0x5a)
					|| (character >= 0x61 && character <= 0x7a)
					|| (character >= 0x30 && character <= 0x39)
					|| character == '.' || character == '_' || character == '-'
					|| character == ':' || character > 128)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Test whether a string is an XML name. This is currently an incomplete
	 * test, in that it treats all non-ASCII characters as being valid in names.
	 * 
	 * @param str
	 *            Description of the Parameter
	 * @return The validName value
	 */

	private boolean isValidName(final String str) {
		if (!isValidNMTOKEN(str)) {
			return false;
		}
		final int character = str.charAt(0);
		return !((character >= 0x30 && character <= 0x39) || character == '.' || character == '-');
	}

	/**
	 * AttributeDetails is a data structure to keep information about attribute
	 * types
	 * 
	 * @author jchamblee
	 */

	private class AttributeDetails {
		// true if all the attribute values are valid NMTOKENs

		/**
		 * Constructor for the AttributeDetails object
		 * 
		 * @param name
		 *            Description of the Parameter
		 */
		public AttributeDetails(final String name) {
			this.name = name;
			this.occurrences = 0;
			this.values = new BinaryTree();
			this.allNames = true;
			this.allNMTOKENs = true;
		}

		// true if all the attribute values are valid names

		boolean allNMTOKENs;
		// used as a set
		boolean allNames;
		String name;
		int occurrences;
		BinaryTree values;
	}

	// end of inner class ElemHandler

	/**
	 * Description of the Class
	 * 
	 * @author jchamblee
	 */
	private class CharHandler implements NodeHandler {

		/**
		 * Description of the Method
		 * 
		 * @return Description of the Return Value
		 */
		public boolean needsStackFrame() {
			return false;
		}

		/**
		 * Handle character data. Make a note whether significant character data
		 * is found in the element
		 * 
		 * @param nodeInfo
		 *            Description of the Parameter
		 * @param context
		 *            Description of the Parameter
		 * @exception TransformerException
		 *                Description of the Exception
		 */
		public void start(final NodeInfo nodeInfo, final Context context)
				throws TransformerException {
			final String str = nodeInfo.getStringValue();
			final NodeInfo parent = nodeInfo.getParent();
			if (str.trim().length() > 0) {
				final ElementDetails elementDetails = (ElementDetails) context
						.getController().getUserData(parent, "ed");
				elementDetails.hasCharacterContent = true;
			}
		}
	}

	/**
	 * ChildDetails records information about the presence of a child element
	 * within its parent element. If the parent element is sequenced, then the
	 * child elements always occur in sequence with the given frequency.
	 * 
	 * @author jchamblee
	 */

	private class ChildDetails {
		String name;
		boolean optional;
		int position;
		boolean repeatable;
	}

	// ///////////////////////
	// inner classes //
	// ///////////////////////

	/**
	 * Element handler processes each element in turn
	 * 
	 * @author jchamblee
	 */

	private class ElemHandler implements NodeHandler {

		/**
		 * Description of the Method
		 * 
		 * @return Description of the Return Value
		 */
		public boolean needsStackFrame() {
			return false;
		}

		/**
		 * Handle the start of an element
		 * 
		 * @param node
		 *            Description of the Parameter
		 * @param context
		 *            Description of the Parameter
		 * @exception TransformerException
		 *                Description of the Exception
		 */
		public void start(final NodeInfo node, final Context context)
				throws TransformerException {
			final AxisEnumeration atts = node.getEnumeration(Axis.ATTRIBUTE,
					AnyNodeTest.getInstance());
			final String name = node.getDisplayName();
			final Controller ctrl = context.getController();

			// create an entry in the Element List, or locate the existing entry
			ElementDetails elementDetails = (ElementDetails) elementList
					.get(name);
			if (elementDetails == null) {
				elementDetails = new ElementDetails(name);
				elementList.put(name, elementDetails);
			}

			// retain the associated element details object
			ctrl.setUserData(node, "ed", elementDetails);

			// initialise sequence numbering of child element types
			ctrl.setUserData(node, "seq", Integer.valueOf(-1));

			// count occurrences of this element type
			elementDetails.occurrences++;

			// Handle the attributes accumulated for this element.
			// Merge the new attribute list into the existing list for the
			// element

			while (atts.hasMoreElements()) {
				final NodeInfo att = atts.nextElement();
				final String attName = att.getDisplayName();
				final String val = att.getStringValue();

				AttributeDetails attributeDetails = (AttributeDetails) elementDetails.attributes
						.get(attName);
				if (attributeDetails == null) {
					attributeDetails = new AttributeDetails(attName);
					elementDetails.attributes.put(attName, attributeDetails);
				}

				attributeDetails.values.put(val, Boolean.TRUE);
				// this is a dummy value to indicate presence
				if (!isValidName(val)) {
					attributeDetails.allNames = false;
				}
				// check if attribute value is a valid name
				if (!isValidNMTOKEN(val)) {
					attributeDetails.allNMTOKENs = false;
				}
				attributeDetails.occurrences++;
			}

			// now keep track of the nesting and sequencing of child elements
			final NodeInfo parent = (NodeInfo) node.getParent();
			if (parent.getNodeType() == NodeInfo.ELEMENT) {
				final ElementDetails parentDetails = (ElementDetails) ctrl
						.getUserData(parent, "ed");
				int seq = ((Integer) ctrl.getUserData(parent, "seq"))
						.intValue();

				// for sequencing, we're interested in consecutive groups of the
				// same child element type
				if (isFirstInGroup(node)) {
					seq++;
					ctrl.setUserData(parent, "seq", Integer.valueOf(seq));
				}

				// if we've seen this child of this parent before, get the
				// details
				final BinaryTree children = parentDetails.children;
				ChildDetails childDetails = (ChildDetails) children.get(name);
				if (childDetails == null) {
					// this is the first time we've seen this child belonging to
					// this parent
					childDetails = new ChildDetails();
					childDetails.name = name;
					childDetails.position = seq;
					childDetails.repeatable = false;
					childDetails.optional = false;
					children.put(name, childDetails);
					parentDetails.childseq.addElement(childDetails);

					// if the first time we see this child is not on the first
					// instance of the parent,
					// then we allow it as an optional element
					if (parentDetails.occurrences != 1) {
						childDetails.optional = true;
					}
				} else {

					// if it's the first occurrence of the parent element, and
					// we've seen this
					// child before, and it's the first of a new group, then the
					// child occurrences are
					// not consecutive
					if (parentDetails.occurrences == 1 && isFirstInGroup(node)) {
						parentDetails.sequenced = false;
					}

					// check whether the position of this group of children in
					// this parent element is
					// the same as its position in previous instances of the
					// parent.
					if (parentDetails.childseq.size() <= seq
							|| !((ChildDetails) parentDetails.childseq
									.elementAt(seq)).name.equals(name)) {
						parentDetails.sequenced = false;
					}
				}

				// if there's more than one child element, mark it as repeatable
				if (!isFirstInGroup(node)) {
					childDetails.repeatable = true;
				}
			}

			ctrl.applyTemplates(context, null, null, null);

			//
			// End of element. If sequenced, check that all expected children
			// are accounted for.
			//

			// if the number of child element groups in this parent element is
			// less than the
			// number in previous elements, then the absent children are marked
			// as optional
			if (elementDetails.sequenced) {
				final int seq = ((Integer) ctrl.getUserData(node, "seq"))
						.intValue();
				for (int i = seq + 1; i < elementDetails.childseq.size(); i++) {
					((ChildDetails) elementDetails.childseq.elementAt(i)).optional = true;
				}
			}
		}

	}

	/**
	 * ElementDetails is a data structure to keep information about element
	 * types
	 * 
	 * @author jchamblee
	 */

	private class ElementDetails {

		/**
		 * Constructor for the ElementDetails object
		 * 
		 * @param name
		 *            Description of the Parameter
		 */
		public ElementDetails(final String name) {
			this.name = name;
			this.occurrences = 0;
			this.hasCharacterContent = false;
			this.sequenced = true;
			this.children = new BinaryTree();
			this.childseq = new Vector();
			this.attributes = new BinaryTree();
		}

		BinaryTree attributes;
		BinaryTree children;
		Vector childseq;
		boolean hasCharacterContent;
		String name;
		int occurrences;
		boolean sequenced;
	}

	private final BinaryTree elementList;

}
// end of outer class DTDGenerator

