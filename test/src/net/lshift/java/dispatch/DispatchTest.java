/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.lshift.java.dispatch;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DispatchTest
    extends TestCase
{
    public static TestSuite suite()
    {
	TestSuite suite = new TestSuite("dispatch");
	suite.addTest(JavaC3Test.suite());
	// suite.addTest(ImplementsFirstDirectSuperclassesTest.suite());
	suite.addTest(DynamicDispatchTest.suite());
	return suite;
    }
}
