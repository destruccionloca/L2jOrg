package org.l2j.gameserver.data.xml.parser;

import org.dom4j.Element;
import org.l2j.commons.data.xml.AbstractParser;
import org.l2j.gameserver.data.xml.holder.ItemHolder;
import org.l2j.gameserver.data.xml.holder.MultiSellHolder;
import org.l2j.gameserver.model.MultiSellListContainer;
import org.l2j.gameserver.model.MultiSellListContainer.MultisellType;
import org.l2j.gameserver.model.base.MultiSellEntry;
import org.l2j.gameserver.model.base.MultiSellIngredient;
import org.l2j.gameserver.model.items.ItemInstance;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.templates.item.ItemTemplate;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;

import static org.l2j.commons.configuration.Configurator.getSettings;
import static org.l2j.gameserver.Contants.Items;

/**
 * @author VISTALL
 * @date 15:42/01.08.2011
 */
public class MultiSellParser extends AbstractParser<MultiSellHolder>
{
	private static final MultiSellParser _instance = new MultiSellParser();

	public static MultiSellParser getInstance()
	{
		return _instance;
	}

	protected MultiSellParser()
	{
		super(MultiSellHolder.getInstance());
	}

	@Override
	public Path getXMLPath() {
		return getSettings(ServerSettings.class).dataPackRootPath().resolve("data/multisell");
	}

	@Override
	public Path getCustomXMLPath() {
		return getSettings(ServerSettings.class).dataPackRootPath().resolve("custom/multisell");
	}

	@Override
	public String getDTDFileName()
	{
		return "multisell.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		final int listId = Integer.parseInt(_currentFile.replace(".xml", ""));
		boolean showAll = true;
		boolean keepEnchanted = false;
		boolean noTax = false;
		boolean noKey = false;
		MultisellType type = MultisellType.NORMAL;

		Element configElement = rootElement.element("config");
		if(configElement != null)
		{
			if(configElement.attributeValue("show_all") != null)
				showAll = Boolean.parseBoolean(configElement.attributeValue("show_all"));
			if(configElement.attributeValue("keep_enchanted") != null)
				keepEnchanted = Boolean.parseBoolean(configElement.attributeValue("keep_enchanted"));
			if(configElement.attributeValue("no_tax") != null)
				noTax = Boolean.parseBoolean(configElement.attributeValue("no_tax"));
			if(configElement.attributeValue("no_key") != null)
				noKey = Boolean.parseBoolean(configElement.attributeValue("no_key"));
			if(configElement.attributeValue("type") != null)
				type = MultisellType.valueOf(configElement.attributeValue("type").toUpperCase());
		}

		MultiSellListContainer list = new MultiSellListContainer();
		list.setShowAll(showAll);
		list.setNoTax(noTax);
		list.setKeepEnchant(keepEnchanted);
		list.setNoKey(noKey);
		list.setType(type);

		int entryId = 0;
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if("item".equalsIgnoreCase(element.getName()))
			{
				MultiSellEntry e = parseEntry(element, listId);
				if(e != null)
				{
					e.setEntryId(entryId++);
					list.addEntry(e);
				}
			}
		}
		getHolder().addMultiSellListContainer(listId, list);
	}

	protected MultiSellEntry parseEntry(Element n, int multiSellId)
	{
		MultiSellEntry entry = new MultiSellEntry();

		for(Iterator<Element> iterator = n.elementIterator(); iterator.hasNext();)
		{
			Element d = iterator.next();
			if("ingredient".equalsIgnoreCase(d.getName()))
			{
				int id = Integer.parseInt(d.attributeValue("id"));
				long count = Long.parseLong(d.attributeValue("count"));
				entry.addIngredient(new MultiSellIngredient(id, count));
			}
			else if("production".equalsIgnoreCase(d.getName()))
			{
				int id = Integer.parseInt(d.attributeValue("id"));
				long count = Long.parseLong(d.attributeValue("count"));
				int chance = d.attributeValue("chance") == null ? 0 : Integer.parseInt(d.attributeValue("chance"));

				int flags = 0;
				String[] flagsArray = d.attributeValue("flags") == null ? null : d.attributeValue("flags").split(";");
				if(flagsArray != null)
				{
					for(String flag : flagsArray)
					{
						switch(flag)
						{
							case "FLAG_NO_DROP":
								flags |= ItemInstance.FLAG_NO_DROP;
								break;
							case "FLAG_NO_TRADE":
								flags |= ItemInstance.FLAG_NO_TRADE;
								break;
							case "FLAG_NO_TRANSFER":
								flags |= ItemInstance.FLAG_NO_TRANSFER;
								break;
							case "FLAG_NO_CRYSTALLIZE":
								flags |= ItemInstance.FLAG_NO_CRYSTALLIZE;
								break;
						}
					}
				}
				int durablity = d.attributeValue("durablity") == null ? -1 : Integer.parseInt(d.attributeValue("durablity"));
				int enchant = d.attributeValue("enchant") == null ? 0 : Integer.parseInt(d.attributeValue("enchant"));
				entry.addProduct(new MultiSellIngredient(id, count, chance, flags, durablity, enchant));
			}
		}

		if(entry.getIngredients().isEmpty() || entry.getProduction().isEmpty())
		{
			logger.warn("MultiSell [" + multiSellId + "] is empty!");
			return null;
		}

		for(MultiSellIngredient ingridient : entry.getIngredients())
		{
			if(ingridient.getItemId() == Items.ADENA && ingridient.getItemCount() == -1)
			{
				long price = 0;
				for(MultiSellIngredient product : entry.getProduction())
				{
					ItemTemplate item = ItemHolder.getInstance().getTemplate(product.getItemId());
					if(item == null)
						continue;

					price += item.getReferencePrice() * product.getItemCount();
				}
				ingridient.setItemCount(price);
			}
			else if(ingridient.getItemCount() <= 0)
			{
				logger.warn("MultiSell [" + multiSellId + "] ingridient ID[" + ingridient.getItemId() + "] has negative item count!");
				return null;
			}
		}

		if(entry.getIngredients().size() == 1 && entry.getProduction().size() == 1 && entry.getIngredients().get(0).getItemId() == 57)
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(entry.getProduction().get(0).getItemId());
			if(item == null)
			{
				logger.warn("MultiSell [" + multiSellId + "] Production [" + entry.getProduction().get(0).getItemId() + "] not found!");
				return null;
			}

			//if(item.getReferencePrice() > entry.getIngredients().get(0).getItemCount()) fuck it it's spams all the time
			//logger.warn("MultiSell [" + multiSellId + "] Production '" + item.getName() + "' [" + entry.getProduction().get(0).getItemId() + "] price is lower than referenced | " + item.getReferencePrice() + " > " + entry.getIngredients().get(0).getItemCount());
		}

		return entry;
	}
}