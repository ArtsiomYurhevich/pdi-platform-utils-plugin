/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.di.baserver.utils.inspector;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.baserver.utils.CallEndpointMeta;
import org.pentaho.di.baserver.utils.web.Http;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WadlParserTest {

  private WadlParser wadlParser, wadlParserSpy, testableWadlParser;
  private Document doc;

  private static String DEPRECATED = BaseMessages.getString( CallEndpointMeta.class, "WadlParser.endpoint.deprecated" );
  private static String PRIVATE = BaseMessages.getString( CallEndpointMeta.class, "WadlParser.endpoint.private" );

  private static String TEST_DATA_DEPRECATED =
      "<visibility>Public</visibility><deprecated>true</deprecated><documentation>Retrieve the all the job(s) visible"
          + " to the current users.</documentation>";
  private static String TEST_DATA_RESULT_DEPRECATED = DEPRECATED
      + "Retrieve the all the job(s) visible to the current users.";

  private static String TEST_DATA =
      "<visibility>Public</visibility><documentation>Retrieve the all the job(s) visible to the current users"
          + ".</documentation>";
  private static String TEST_DATA_RESULT = "Retrieve the all the job(s) visible to the current users.";

  private static String TEST_DATA_PRIVATE =
      "<visibility>Private</visibility><deprecated>true</deprecated><documentation>Retrieve the all the job(s) "
          + "visible to the current users.</documentation>";

  private static String TEST_DATA_MULTILINE =
      "<visibility>Public</visibility><deprecated>true</deprecated><documentation>Return a list of the permission "
          + "roles in the platform.\r\n"
          + "\r\n"
          + " <p><b>Example Request:</b><br />\r\n"
          + "    GET pentaho/api/userrolelist/permission-roles\r\n"
          + " </p></documentation>";

  private static String TEST_DATA_MULTILINE_RESULT = DEPRECATED
      + "Return a list of the permission roles in the platform.\r\n" + "\r\n" + " <p><b>Example Request:</b><br />\r\n"
      + "    GET pentaho/api/userrolelist/permission-roles\r\n" + " </p>";

  private static class TestableWadlParser extends WadlParser {

    @Override
    public boolean isSupported( String in ) {
      return super.isSupported( in );
    }

    @Override
    public boolean isDeprecated( String in ) {
      return super.isDeprecated( in );
    }

    @Override
    public String extractComment( String in ) {
      return super.extractComment( in );
    }
  }

  @Before
  public void setUp() throws Exception {
    wadlParser = new WadlParser();
    wadlParserSpy = spy( wadlParser );
    testableWadlParser = new TestableWadlParser();

    InputStream is = new FileInputStream( new File( "test-res/wadl.xml" ) );
    SAXReader reader = new SAXReader();
    doc = reader.read( is );
  }

  @Test
  public void testGetEndpoints() throws Exception {
    Collection<Endpoint> endpointList = wadlParserSpy.getEndpoints( doc );
    Node resources = doc.selectSingleNode( "/application/child::*[local-name() = 'resources' ]" );

    verify( wadlParserSpy, times( 1 ) ).parseResources( eq( resources ), anyString() );
    assertEquals( endpointList.size(), 142 );

    Endpoint endpoint = (Endpoint) endpointList.toArray()[ 0 ];
    assertEquals( endpoint.getHttpMethod(), Http.POST );
    assertEquals( endpoint.getId(), "addBlockout" );
    assertEquals( endpoint.getPath(), "/scheduler/blockout/add" );
    assertEquals( endpoint.getParamDescriptions().size(), 0 );
    assertEquals( endpoint.isDeprecated(), false );
    assertEquals( endpoint.isSupported(), true );
    assertEquals( endpoint.getDocumentation().isEmpty(), false );

    endpoint = (Endpoint) endpointList.toArray()[ 1 ];
    assertEquals( endpoint.getHttpMethod(), Http.PUT );
    assertEquals( endpoint.getId(), "assignAllRolesToUser" );
    assertEquals( endpoint.getPath(), "/userroledao/assignAllRolesToUser" );
    Collection<ParamDescription> paramDescriptionList = endpoint.getParamDescriptions();
    assertEquals( paramDescriptionList.size(), 2 );
    assertEquals( ( (ParamDescription) paramDescriptionList.toArray()[ 0 ] ).getName(), "tenant" );
    assertEquals( ( (ParamDescription) paramDescriptionList.toArray()[ 0 ] ).getContentType(), "xs:string" );
    assertEquals( ( (ParamDescription) paramDescriptionList.toArray()[ 1 ] ).getName(), "userName" );
    assertEquals( ( (ParamDescription) paramDescriptionList.toArray()[ 1 ] ).getContentType(), "xs:string" );
    assertEquals( endpoint.isDeprecated(), false );
    assertEquals( endpoint.isSupported(), false );
    assertEquals( endpoint.getDocumentation().isEmpty(), false );

    endpoint = (Endpoint) endpointList.toArray()[ 69 ];
    assertEquals( endpoint.getHttpMethod(), Http.GET );
    assertEquals( endpoint.getId(), "getAllRoles" );
    assertEquals( endpoint.getPath(), "/userrolelist/allRoles" );
    assertEquals( endpoint.getParamDescriptions().size(), 0 );
    assertEquals( endpoint.isDeprecated(), false );
    assertEquals( endpoint.isSupported(), true );
    assertEquals( endpoint.getDocumentation().isEmpty(), false );

    assertEquals( wadlParserSpy.getEndpoints( mock( Document.class ) ).size(), 0 );
  }

  @Test
  public void testParseResources() throws Exception {
    final Node resourceNode = mock( Node.class );
    final String parentPath = "parentPath";

    when( resourceNode.valueOf( "@path" ) ).thenReturn( "" );

    final String id = "id";
    final Http httpMethod = Http.GET;
    final Node mockNode = createMockNode( id, httpMethod );
    when( resourceNode.selectNodes( anyString() ) ).thenReturn( new ArrayList() {
      {
        add( mockNode );
      }
    }, new ArrayList() );
    final Collection<Endpoint> endpoints = wadlParserSpy.parseResources( resourceNode, parentPath );

    verify( wadlParserSpy, times( 1 ) ).parseMethod( any( Node.class ), eq( parentPath ) );
    assertNotNull( endpoints );
    assertEquals( endpoints.size(), 1 );
    final Endpoint endpoint = endpoints.iterator().next();
    assertEquals( endpoint.getId(), id );
    assertEquals( endpoint.getHttpMethod(), httpMethod );
    assertEquals( endpoint.getPath(), parentPath );
  }

  @Test
  public void testParseMethod() {
    final String id = "id";
    final Http httpMethod = Http.GET;
    final String path = "path";

    final Endpoint endpoint = wadlParserSpy.parseMethod( createMockNode( id, httpMethod ), path );
    assertNotNull( endpoint );
    assertEquals( endpoint.getId(), id );
    assertEquals( endpoint.getHttpMethod(), httpMethod );
    assertEquals( endpoint.getPath(), path );
  }

  @Test
  public void testParseQueryParam() {
    final String id = "id";
    final Http httpMethod = Http.GET;
    final Node mockNode = createMockNode( id, httpMethod );

    final String name = "name";
    final String type = "type";
    doReturn( name ).when( mockNode ).valueOf( "@name" );
    doReturn( type ).when( mockNode ).valueOf( "@type" );

    final ParamDescription paramDescription = wadlParserSpy.parseParam( mockNode, ParamDescription.ParamType.QUERY );
    assertNotNull(paramDescription);
    assertEquals( paramDescription.getName(), name );
    assertEquals( paramDescription.getContentType(), type );
  }

  @Test
  public void testSanitizePath() {
    final String path = "path";
    String sanitizePath = wadlParserSpy.sanitizePath( path );
    assertEquals( path, sanitizePath );

    final String path1 = "/path";
    sanitizePath = wadlParserSpy.sanitizePath( path1 );
    assertEquals( path, sanitizePath );

    final String path2 = "path/";
    sanitizePath = wadlParserSpy.sanitizePath( path2 );
    assertEquals( path, sanitizePath );

    final String path3 = "/path/";
    sanitizePath = wadlParserSpy.sanitizePath( path3 );
    assertEquals( path, sanitizePath );
  }

  @Test
  public void testShortPath() {
    final String path = "/path";
    String shortPath = wadlParserSpy.shortPath( path );
    assertEquals( path, shortPath );

    String apiPath = "somePath/api" + path;
    shortPath = wadlParserSpy.shortPath( apiPath );
    assertEquals( path, shortPath );
  }

  private Node createMockNode( String id, Http httpMethod ) {
    final Node node = mock( Node.class );
    doReturn( id ).when( node ).valueOf( "@id" );
    doReturn( httpMethod.toString() ).when( node ).valueOf( "@name" );

    return node;
  }

  @Test
  public void testParseDocDeprecated() throws Exception {
    TestableWadlParser testableWadlParser = new TestableWadlParser();

    Assert.assertEquals( true, testableWadlParser.isDeprecated( "<deprecated>true</deprecated>" ) );
    Assert.assertEquals( true, testableWadlParser.isDeprecated( "<deprecated>TRUE</deprecated>" ) );
    Assert.assertEquals( true, testableWadlParser.isDeprecated( "<deprecated>True</deprecated>" ) );
    Assert.assertEquals( false, testableWadlParser.isDeprecated( "<deprecated>TrUe</deprecated>" ) );
    Assert.assertEquals( false, testableWadlParser.isDeprecated( "<deprecated>false</deprecated>" ) );
    Assert.assertEquals( false, testableWadlParser.isDeprecated( "" ) );
    Assert.assertEquals( true, testableWadlParser.isDeprecated( TEST_DATA_MULTILINE ) );
  }

}
