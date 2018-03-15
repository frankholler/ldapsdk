/*
 * Copyright 2015-2018 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2015-2018 Ping Identity Corporation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds.extensions;



import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.unboundid.ldap.sdk.LDAPSDKTestCase;



/**
 * This class provides a set of test cases for the set notification destination
 * change type enum.
 */
public final class SetNotificationDestinationChangeTypeTestCase
       extends LDAPSDKTestCase
{
  /**
   * Provides test coverage for the change types enum.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testChangeTypes()
         throws Exception
  {
    assertEquals(SetNotificationDestinationChangeType.REPLACE.intValue(), 0);
    assertEquals(SetNotificationDestinationChangeType.valueOf(0),
         SetNotificationDestinationChangeType.REPLACE);

    assertEquals(SetNotificationDestinationChangeType.ADD.intValue(), 1);
    assertEquals(SetNotificationDestinationChangeType.valueOf(1),
         SetNotificationDestinationChangeType.ADD);

    assertEquals(SetNotificationDestinationChangeType.DELETE.intValue(), 2);
    assertEquals(SetNotificationDestinationChangeType.valueOf(2),
         SetNotificationDestinationChangeType.DELETE);

    for (final SetNotificationDestinationChangeType t :
         SetNotificationDestinationChangeType.values())
    {
      assertEquals(SetNotificationDestinationChangeType.valueOf(t.intValue()),
           t);
      assertEquals(SetNotificationDestinationChangeType.valueOf(t.name()),
           t);
    }

    assertNull(SetNotificationDestinationChangeType.valueOf(3));

    try
    {
      SetNotificationDestinationChangeType.valueOf("undefined");
      fail("Expected an exception from an undefined string valueOf");
    }
    catch (final IllegalArgumentException e)
    {
      // This was expected.
    }
  }



  /**
   * Tests the {@code forName} method with automated tests based on the actual
   * name of the enum values.
   *
   * @throws  Exception  If an unexpected problem occurs.
   */
  @Test()
  public void testForNameAutomated()
         throws Exception
  {
    for (final SetNotificationDestinationChangeType value :
         SetNotificationDestinationChangeType.values())
    {
      for (final String name : getNames(value.name()))
      {
        assertNotNull(SetNotificationDestinationChangeType.forName(name));
        assertEquals(SetNotificationDestinationChangeType.forName(name), value);
      }
    }

    assertNull(SetNotificationDestinationChangeType.forName(
         "some undefined name"));
  }



  /**
   * Retrieves a set of names for testing the {@code forName} method based on
   * the provided set of names.
   *
   * @param  baseNames  The base set of names to use to generate the full set of
   *                    names.  It must not be {@code null} or empty.
   *
   * @return  The full set of names to use for testing.
   */
  private static Set<String> getNames(final String... baseNames)
  {
    final HashSet<String> nameSet = new HashSet<>(10);
    for (final String name : baseNames)
    {
      nameSet.add(name);
      nameSet.add(name.toLowerCase());
      nameSet.add(name.toUpperCase());

      final String nameWithDashesInsteadOfUnderscores = name.replace('_', '-');
      nameSet.add(nameWithDashesInsteadOfUnderscores);
      nameSet.add(nameWithDashesInsteadOfUnderscores.toLowerCase());
      nameSet.add(nameWithDashesInsteadOfUnderscores.toUpperCase());

      final String nameWithUnderscoresInsteadOfDashes = name.replace('-', '_');
      nameSet.add(nameWithUnderscoresInsteadOfDashes);
      nameSet.add(nameWithUnderscoresInsteadOfDashes.toLowerCase());
      nameSet.add(nameWithUnderscoresInsteadOfDashes.toUpperCase());

      final StringBuilder nameWithoutUnderscoresOrDashes = new StringBuilder();
      for (final char c : name.toCharArray())
      {
        if ((c != '-') && (c != '_'))
        {
          nameWithoutUnderscoresOrDashes.append(c);
        }
      }
      nameSet.add(nameWithoutUnderscoresOrDashes.toString());
      nameSet.add(nameWithoutUnderscoresOrDashes.toString().toLowerCase());
      nameSet.add(nameWithoutUnderscoresOrDashes.toString().toUpperCase());
    }

    return nameSet;
  }
}
