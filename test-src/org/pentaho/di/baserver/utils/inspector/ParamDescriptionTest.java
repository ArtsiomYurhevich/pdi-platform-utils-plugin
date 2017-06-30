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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class ParamDescriptionTest {
  private ParamDescription paramDescription;


  @Before
  public void setup() {
    paramDescription = new ParamDescription();
  }

  @Test
  public void testGet() {
    String name = "paramName";
    paramDescription.setName( name );
    assertEquals( paramDescription.getName(), name );

    String type = "String";
    paramDescription.setContentType( type );
    assertEquals( paramDescription.getContentType(), type );
  }

  @Test
  public void testCompareTo() {
    String name = "paramName",
        name2 = "paramName1234";
    paramDescription.setName( name );

    assertEquals( paramDescription.compareTo(paramDescription), name.compareTo( name ) );

    ParamDescription paramDescription2 = new ParamDescription();
    paramDescription2.setName( name2 );

    assertEquals( paramDescription.compareTo(paramDescription2), name.compareTo( name2 ) );
  }

  @Test
  public void testEquals() {
    String name = "paramName";
    paramDescription.setName( name );

    assertTrue( paramDescription.equals(paramDescription) );

    ParamDescription paramDescription2 = new ParamDescription();
    paramDescription2.setName( name );
    assertTrue( paramDescription.equals(paramDescription2) );

    assertFalse( paramDescription.equals( null ) );
    assertFalse( paramDescription.equals( new Endpoint() ) );

    paramDescription2.setName( "12345" );
    assertFalse( paramDescription.equals(paramDescription2) );
  }

  @Test
  public void testHashCode() {
    String name = "paramName";
    paramDescription.setName( name );

    assertEquals( name.hashCode(), paramDescription.hashCode() );
  }

}
