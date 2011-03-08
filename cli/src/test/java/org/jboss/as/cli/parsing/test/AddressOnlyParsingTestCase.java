/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.cli.parsing.test;

import java.util.Iterator;

import org.jboss.as.cli.operation.OperationRequestAddress;
import org.jboss.as.cli.operation.OperationRequestAddress.Node;
import org.jboss.as.cli.operation.impl.DefaultOperationCallbackHandler;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestAddress;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestParser;
import org.junit.Test;

import junit.framework.TestCase;

/**
 *
 * @author Alexey Loubyansky
 */
public class AddressOnlyParsingTestCase extends TestCase {

    private DefaultOperationRequestParser parser = new DefaultOperationRequestParser();

    @Test
    public void testNodeTypeOnly() throws Exception {
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler();

        parser.parse("subsystem", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertTrue(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("subsystem", node.getType());
        assertNull(node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testNodeTypeNameSeparator() throws Exception {
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler();

        parser.parse("subsystem=", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertTrue(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertTrue(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("subsystem", node.getType());
        assertNull(node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testNodeTypeOnlyWithPrefixNode() throws Exception {
        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNode("a", "b");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse("subsystem", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertTrue(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("a", node.getType());
        assertEquals("b", node.getName());
        assertTrue(i.hasNext());
        node = i.next();
        assertNotNull(node);
        assertEquals("subsystem", node.getType());
        assertNull(node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testNodeNameOnly() throws Exception {
        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNodeType("a");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse("b", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("a", node.getType());
        assertEquals("b", node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testNodeNameOnlyWithNodeSeparator() throws Exception {
        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNodeType("a");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse("b/", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertTrue(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("a", node.getType());
        assertEquals("b", node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testOneNode() throws Exception {
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler();

        parser.parse("subsystem=logging", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("subsystem", node.getType());
        assertEquals("logging", node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testOneNodeWithNodeSeparator() throws Exception {
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler();

        parser.parse("subsystem=logging/", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertTrue(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("subsystem", node.getType());
        assertEquals("logging", node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testEndsOnType() throws Exception {
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler();

        parser.parse("a=b/c", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertTrue(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("a", node.getType());
        assertEquals("b", node.getName());
        assertTrue(i.hasNext());
        node = i.next();
        assertNotNull(node);
        assertEquals("c", node.getType());
        assertNull(node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testNodeWithPrefix() throws Exception {
        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNode("a", "b");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse("c=d", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("a", node.getType());
        assertEquals("b", node.getName());
        assertTrue(i.hasNext());
        node = i.next();
        assertNotNull(node);
        assertEquals("c", node.getType());
        assertEquals("d", node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testRootOnly() throws Exception {

        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNode("a", "b");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse("/", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        // root is also a separator
        assertTrue(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertFalse(i.hasNext());
    }

    @Test
    public void testRootInCombination() throws Exception {

        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNode("a", "b");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        //parser.parse("c=d,~,e=f", handler);
        parser.parse("/e=f", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("e", node.getType());
        assertEquals("f", node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testParentOnly() throws Exception {

        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNode("a", "b");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse("..", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertFalse(i.hasNext());
    }

    @Test
    public void testParentInCombination() throws Exception {

        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler();

        parser.parse("c=d/../e=f", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("e", node.getType());
        assertEquals("f", node.getName());
        assertFalse(i.hasNext());
    }

    @Test
    public void testToTypeOnly() throws Exception {

        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNode("a", "b");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse(".type", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertTrue(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("a", node.getType());
        assertNull(node.getName());
    }

    @Test
    public void testToTypeInCombination() throws Exception {

        OperationRequestAddress prefix = new DefaultOperationRequestAddress();
        prefix.toNodeType("a");
        DefaultOperationCallbackHandler handler = new DefaultOperationCallbackHandler(prefix);

        parser.parse("b/.type/c", handler);

        assertTrue(handler.hasAddress());
        assertFalse(handler.hasOperationName());
        assertFalse(handler.hasProperties());
        assertFalse(handler.endsOnAddressOperationNameSeparator());
        assertFalse(handler.endsOnArgumentListStart());
        assertFalse(handler.endsOnArgumentSeparator());
        assertFalse(handler.endsOnArgumentValueSeparator());
        assertFalse(handler.endsOnNodeSeparator());
        assertFalse(handler.endsOnNodeTypeNameSeparator());
        assertFalse(handler.isRequestComplete());

        OperationRequestAddress address = handler.getAddress();
        assertFalse(address.endsOnType());
        Iterator<Node> i = address.iterator();
        assertTrue(i.hasNext());
        Node node = i.next();
        assertNotNull(node);
        assertEquals("a", node.getType());
        assertEquals("c", node.getName());
    }

}
