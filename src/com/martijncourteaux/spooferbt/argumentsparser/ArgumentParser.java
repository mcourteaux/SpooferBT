package com.martijncourteaux.spooferbt.argumentsparser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArgumentParser
{
	private String usage;
	private Map<String, Argument> args = new HashMap<String, Argument>();

	public ArgumentParser()
	{

	}

	public void addArgument(String name, Class<?> valueType, Object defaultValue)
	{
		Argument arg = new Argument();
		arg.name = name;
		arg.valueType = valueType;
		arg.value = defaultValue;
		arg.defaultValue = defaultValue;
		args.put(name, arg);
	}

	public void parse(String[] arguments)
	{
		for (int i = 0; i < arguments.length; ++i)
		{
			String argName = arguments[i];
			Argument arg = args.get(argName);
			if (arg == null)
			{
				System.out.println("Unknown argument: " + argName);
				usage();
				System.exit(0);
			}
			arg.present = true;
			if (arg.valueType != null)
			{
				String value = arguments[++i];
				arg.setValue(value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getOrUsage(String name, Class<T> type)
	{
		Argument a = args.get(name);
		if (a == null)
		{
			System.out.println("No argument named: " + name + " found!");
			System.exit(1);
		}
		if (a.valueType != type)
			return null;
		if (a.present)
		{
			return (T) a.value;
		} else if (a.defaultValue != null)
		{
			return (T) a.defaultValue;

		} else
		{
			System.out.println("Argument not found! " + name);
			usage();
			System.exit(0);
			return null;
		}
	}

	public boolean isPresent(String name)
	{
		Argument a = args.get(name);
		if (a == null)
		{
			System.out.println("No argument named: " + name + " found!");
			System.exit(1);
		}
		return a.present;
	}

	public void usage()
	{
		System.out.println(usage);
	}

	public int oneOfTheseOrUsage(String... names)
	{
		int found = -1;
		for (int i = 0; i < names.length; ++i)
		{
			if (isPresent(names[i]))
			{
				if (found != -1)
				{
					found = -1;
					break;
				} else
				{
					found = i;
				}
			}
		}
		if (found == -1)
		{
			System.out.println("None of these found: " + Arrays.toString(names));
			usage();
			System.exit(0);
		}
		return found;
	}

	public void setUsage(String usage)
	{
		this.usage = usage;
	}

	public int oneOfTheseOr(int defaultOption, String... names)
	{
		for (int i = 0; i < names.length; ++i)
		{
			if (isPresent(names[i]))
			{
				return i;
			}
		}
		return defaultOption;
	}

}
