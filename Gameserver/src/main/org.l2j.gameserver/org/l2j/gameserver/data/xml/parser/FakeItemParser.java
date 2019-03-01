package org.l2j.gameserver.data.xml.parser;

import io.github.joealisson.primitive.lists.IntList;
import io.github.joealisson.primitive.lists.impl.ArrayIntList;
import org.dom4j.Element;
import org.l2j.commons.data.xml.AbstractParser;
import org.l2j.gameserver.data.xml.holder.FakeItemHolder;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.templates.item.ArmorTemplate.ArmorType;
import org.l2j.gameserver.templates.item.ItemGrade;

import java.nio.file.Path;
import java.util.*;

import static org.l2j.commons.configuration.Configurator.getSettings;

public class FakeItemParser extends AbstractParser<FakeItemHolder>
{
	private static FakeItemParser ourInstance = new FakeItemParser();

	public static FakeItemParser getInstance()
	{
		return ourInstance;
	}

	private FakeItemParser()
	{
		super(FakeItemHolder.getInstance());
	}

	@Override
	public Path getXMLPath() {
		return getSettings(ServerSettings.class).dataPackRootPath().resolve("data/fake_players/fake_item.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "fake_item.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if("category".equalsIgnoreCase(element.getName()))
			{
				ItemGrade grade = ItemGrade.valueOf(element.attributeValue("grade"));
				for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
				{
					Element subElement = subIterator.next();
					if("weapons".equalsIgnoreCase(subElement.getName()))
						getHolder().addWeapons(grade, parseItems(subElement));
					else if("armors".equalsIgnoreCase(subElement.getName()))
						getHolder().addArmors(grade, parsePackArmors(subElement));
					else if("accessorys".equalsIgnoreCase(subElement.getName()))
						getHolder().addAccessorys(grade, parsePackAccessorys(subElement));
				}
			}
			else if("classes".equalsIgnoreCase(element.getName()))
			{
				for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
				{
					Element subElement = subIterator.next();
					if("class".equalsIgnoreCase(subElement.getName()))
					{
						int classId = Integer.parseInt(subElement.attributeValue("id"));
						String weaponTypes = subElement.attributeValue("weaponTypes");
						String armorTypes = subElement.attributeValue("armorTypes");
						getHolder().addClassWeaponAndArmors(classId, weaponTypes, armorTypes);
					}
				}
			}
			else if("hair_accessories".equalsIgnoreCase(element.getName()))
				getHolder().addHairAccessories(parseItems(element));
			else if("cloaks".equalsIgnoreCase(element.getName()))
				getHolder().addCloaks(parseItems(element));
		}
	}

	private static IntList parseItems(Element rootElement)
	{
		IntList list = new ArrayIntList();
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if(element.getName().equals("item"))
			{
				int itemId = Integer.parseInt(element.attributeValue("id"));
				list.add(itemId);
			}
		}
		return list;
	}

	private static Map<ArmorType, IntList> parsePackArmors(Element rootElement)
	{
		Map<ArmorType, IntList> map = new HashMap<ArmorType, IntList>();
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if(element.getName().equals("pack"))
			{
				ArmorType type = ArmorType.valueOf(element.attributeValue("type"));
				if(map.get(type) == null)
					map.put(type, new ArrayIntList());

				map.get(type).addAll(parseItems(element));
			}
		}
		return map;
	}

	private static List<IntList> parsePackAccessorys(Element rootElement)
	{
		List<IntList> list = new ArrayList<IntList>();
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if(element.getName().equals("pack"))
				list.add(parseItems(element));
		}
		return list;
	}
}