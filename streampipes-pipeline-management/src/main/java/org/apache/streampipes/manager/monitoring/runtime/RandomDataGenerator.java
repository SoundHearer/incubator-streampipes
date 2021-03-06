/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.streampipes.manager.monitoring.runtime;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.streampipes.model.schema.EventPropertyPrimitive;
import org.apache.streampipes.vocabulary.XSD;

import java.util.Random;

public class RandomDataGenerator {

	private Random random;
	
	public RandomDataGenerator()
	{
		random = new Random();
	}
	
	public Object getValue(EventPropertyPrimitive primitive)
	{
		if (primitive.getRuntimeType().equals(getString())) {
			return RandomStringUtils.randomAlphabetic(10);
		}
		else if (primitive.getRuntimeType().equals(getLong())) {
			return random.nextLong();
		}
		else if (primitive.getRuntimeType().equals(getInt())) {
			return random.nextInt();
		}
		else if (primitive.getRuntimeType().equals(getDouble())) {
			return random.nextDouble();
		}
		else {
			return random.nextBoolean();
		}
	}
	
	private String getString()
	{
		return XSD._string.toString();
	}
	
	private String getLong()
	{
		return XSD._long.toString();
	}
	
	private String getInt()
	{
		return XSD._integer.toString();
	}
	
	private String getDouble()
	{
		return XSD._double.toString();
	}
}
