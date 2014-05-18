package com.martijncourteaux.spooferbt.argumentsparser;

public class Argument
{

	public String name;
	public Class<?> valueType;
	public Object defaultValue;
	
	public Object value;
	public boolean present;
	
	public void setValue(String str)
	{
		if (valueType == String.class)
		{
			value = str;
		} else if (valueType == Integer.class)
		{
			value = Integer.parseInt(str);
		} else if (valueType == Boolean.class)
		{
			value = Boolean.parseBoolean(str);
		} else
		{
			value = defaultValue;
		}
	}
	
}
