/*
 * Copyright 2009, Niklas Kyster Rasmussen
 *
 * This file is part of jEveAssets.
 *
 * jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package net.nikr.eve.jeveasset.io;

import com.beimin.eveapi.balance.ApiAccountBalance;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.nikr.eve.jeveasset.data.Account;
import net.nikr.eve.jeveasset.data.AssetFilter;
import net.nikr.eve.jeveasset.data.EveAsset;
import net.nikr.eve.jeveasset.data.Human;
import net.nikr.eve.jeveasset.data.MarketstatSettings;
import net.nikr.eve.jeveasset.data.Settings;
import net.nikr.eve.jeveasset.data.UserPrice;
import net.nikr.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class LocalSettingsWriter extends AbstractXmlWriter {

	public static void save(Settings settings){
		Document xmldoc = null;
		try {
			xmldoc = getXmlDocument("settings");
		} catch (XmlException ex) {
			Log.error("Settings not saved "+ex.getMessage(), ex);
		}
		writeBpos(xmldoc, settings.getBpos());
		writeMarketstatSettings(xmldoc, settings.getMarketstatSettings());
		writeFlags(xmldoc, settings.getFlags());
		writeUserPrices(xmldoc, settings.getUserPrices());
		writeColumns(xmldoc, settings.getTableColumnNames(), settings.getTableColumnVisible());
		writeUpdates(xmldoc, settings);
		writeFilters(xmldoc, settings.getAssetFilters());
		writeAccounts(xmldoc, settings.getAccounts());
		try {
			writeXmlFile(xmldoc, Settings.getPathSettings());
		} catch (XmlException ex) {
			Log.error("Settings not saved "+ex.getMessage(), ex);
		}
		Log.info("Settings saved");
	}
	private static void writeBpos(Document xmldoc, List<Integer> bpos){
		Element parentNode = xmldoc.createElementNS(null, "bpos");
		xmldoc.getDocumentElement().appendChild(parentNode);
		for (int a = 0; a < bpos.size(); a++){
			int id = bpos.get(a);
			Element node = xmldoc.createElementNS(null, "bpo");
			node.setAttributeNS(null, "id", String.valueOf(id));
			parentNode.appendChild(node);
		}
	}
	private static void writeUserPrices(Document xmldoc, Map<Integer, UserPrice> userPrices){
		Element parentNode = xmldoc.createElementNS(null, "userprices");
		xmldoc.getDocumentElement().appendChild(parentNode);
		for (Map.Entry<Integer, UserPrice> entry : userPrices.entrySet()){
			UserPrice userPrice = entry.getValue();
			Element node = xmldoc.createElementNS(null, "userprice");
			node.setAttributeNS(null, "name", userPrice.getName());
			node.setAttributeNS(null, "price", String.valueOf(userPrice.getPrice()));
			node.setAttributeNS(null, "typeid", String.valueOf(userPrice.getTypeID()));
			parentNode.appendChild(node);
		}

	}
	private static void writeMarketstatSettings(Document xmldoc, MarketstatSettings marketstatSettings){
		Element parentNode = xmldoc.createElementNS(null, "marketstat");
		parentNode.setAttributeNS(null, "age", String.valueOf(marketstatSettings.getAge()));
		parentNode.setAttributeNS(null, "quantity", String.valueOf(marketstatSettings.getQuantity()));
		parentNode.setAttributeNS(null, "region", String.valueOf(marketstatSettings.getRegion()));
		xmldoc.getDocumentElement().appendChild(parentNode);
	}

	private static void writeFlags(Document xmldoc, Map<String, Boolean> flags){
		Element parentNode = xmldoc.createElementNS(null, "flags");
		xmldoc.getDocumentElement().appendChild(parentNode);
		for (Map.Entry<String, Boolean> entry : flags.entrySet()){
			Element node = xmldoc.createElementNS(null, "flag");
			node.setAttributeNS(null, "key", entry.getKey());
			node.setAttributeNS(null, "enabled", String.valueOf(entry.getValue()));
			parentNode.appendChild(node);
		}
	}

	private static void writeColumns(Document xmldoc, List<String> mainTableColumnNames, List<String> mainTableColumnVisible){
		Element parentNode = xmldoc.createElementNS(null, "columns");
		xmldoc.getDocumentElement().appendChild(parentNode);
		for (int a = 0; a < mainTableColumnNames.size(); a++){
			String column = mainTableColumnNames.get(a);
			boolean visible = mainTableColumnVisible.contains(column);
			Element node = xmldoc.createElementNS(null, "column");
			node.setAttributeNS(null, "name", column);
			node.setAttributeNS(null, "visible", String.valueOf(visible));
			parentNode.appendChild(node);
		}
	}

	private static void writeUpdates(Document xmldoc, Settings settings){
		Element parentNode = xmldoc.createElementNS(null, "updates");
		xmldoc.getDocumentElement().appendChild(parentNode);

		Element node;

		node = xmldoc.createElementNS(null, "update");
		node.setAttributeNS(null, "name", "marketstats");
		node.setAttributeNS(null, "nextupdate", String.valueOf(settings.getMarketstatsNextUpdate().getTime()));
		parentNode.appendChild(node);

		node = xmldoc.createElementNS(null, "update");
		node.setAttributeNS(null, "name", "conquerable station");
		node.setAttributeNS(null, "nextupdate", String.valueOf(settings.getConquerableStationsNextUpdate().getTime()));
		parentNode.appendChild(node);

		Map<Long, Date> corporationNextUpdate = settings.getCorporationsNextUpdate();
		for (Map.Entry<Long, Date> entry : corporationNextUpdate.entrySet()){
			node = xmldoc.createElementNS(null, "update");
			node.setAttributeNS(null, "name", "corporation");
			node.setAttributeNS(null, "corpid", String.valueOf(entry.getKey()));
			node.setAttributeNS(null, "nextupdate", String.valueOf(entry.getValue().getTime()));
			parentNode.appendChild(node);
		}
	}

	private static void writeFilters(Document xmldoc, Map<String, List<AssetFilter>> assetFilters){
		Element parentNode = xmldoc.createElementNS(null, "filters");
		xmldoc.getDocumentElement().appendChild(parentNode);
		for (Map.Entry<String, List<AssetFilter>> entry : assetFilters.entrySet()){
			Element node = xmldoc.createElementNS(null, "filter");
			node.setAttributeNS(null, "name", entry.getKey());
			parentNode.appendChild(node);

			List<AssetFilter> assetFilterFilters = entry.getValue();
			for (int a = 0; a < assetFilterFilters.size(); a++){
				AssetFilter assetFilter = assetFilterFilters.get(a);

				Element childNode = xmldoc.createElementNS(null, "row");
				childNode.setAttributeNS(null, "text", assetFilter.getText());
				childNode.setAttributeNS(null, "column", assetFilter.getColumn());
				childNode.setAttributeNS(null, "mode", assetFilter.getMode());
				childNode.setAttributeNS(null, "and", String.valueOf(assetFilter.isAnd()));
				node.appendChild(childNode);
			}
		}
	}

	private static void writeAccounts(Document xmldoc, List<Account> accounts){
		Element parentNode = xmldoc.createElementNS(null, "accounts");
		xmldoc.getDocumentElement().appendChild(parentNode);

		for (int a = 0; a < accounts.size(); a++){
			Account account = accounts.get(a);
			Element node = xmldoc.createElementNS(null, "account");
			node.setAttributeNS(null, "userid", String.valueOf(account.getUserID()));
			node.setAttributeNS(null, "apikey", account.getApiKey());
			node.setAttributeNS(null, "charactersnextupdate", String.valueOf(account.getCharactersNextUpdate().getTime()));
			parentNode.appendChild(node);
			writeHumans(xmldoc, node, account.getHumans());

		}
	}

	private static void writeHumans(Document xmldoc, Element parentNode, List<Human> humans){
		for (int a = 0; a < humans.size(); a++){
			Human human = humans.get(a);
			Element node = xmldoc.createElementNS(null, "human");
			node.setAttributeNS(null, "id", String.valueOf(human.getCharacterID()));
			node.setAttributeNS(null, "name", human.getName());
			node.setAttributeNS(null, "corporation", human.getCorporation());
			node.setAttributeNS(null, "corpassets", String.valueOf(human.isUpdateCorporationAssets()));
			node.setAttributeNS(null, "show", String.valueOf(human.isShowAssets()));
			node.setAttributeNS(null, "assetsnextupdate", String.valueOf(human.getAssetNextUpdate().getTime()));
			node.setAttributeNS(null, "balancenextupdate", String.valueOf(human.getBalanceNextUpdate().getTime()));
			parentNode.appendChild(node);
			Element childNode = xmldoc.createElementNS(null, "assets");
			node.appendChild(childNode);
			writeAssets(xmldoc, childNode, human.getAssets());
			writeAccountBalances(xmldoc, node,human.getAccountBalances(), false);
			writeAccountBalances(xmldoc, node,human.getCorporationAccountBalances(), true);
		}
	}

	private static void writeAssets(Document xmldoc, Element parentNode, List<EveAsset> assets) {
		for (int a = 0; a < assets.size(); a++){
			EveAsset eveAsset = assets.get(a);
			Element node = xmldoc.createElementNS(null, "asset");
			node.setAttributeNS(null, "name", eveAsset.getName());
			node.setAttributeNS(null, "group", eveAsset.getGroup());
			node.setAttributeNS(null, "category", eveAsset.getCategory());
			node.setAttributeNS(null, "owner", eveAsset.getOwner());
			node.setAttributeNS(null, "count", String.valueOf(eveAsset.getCount()));
			node.setAttributeNS(null, "location", eveAsset.getLocation());
			node.setAttributeNS(null, "container", eveAsset.getContainer());
			node.setAttributeNS(null, "flag", eveAsset.getFlag());
			node.setAttributeNS(null, "price", String.valueOf(eveAsset.getPriceBase()));
			node.setAttributeNS(null, "meta", eveAsset.getMeta());
			node.setAttributeNS(null, "id", String.valueOf(eveAsset.getId()));
			node.setAttributeNS(null, "typeid", String.valueOf(eveAsset.getTypeId()));
			node.setAttributeNS(null, "marketgroup", String.valueOf(eveAsset.isMarketGroup()));
			node.setAttributeNS(null, "corporationasset", String.valueOf(eveAsset.isCorporationAsset()));
			node.setAttributeNS(null, "volume", String.valueOf(eveAsset.getVolume()));
			node.setAttributeNS(null, "region", eveAsset.getRegion());
			node.setAttributeNS(null, "locationid", String.valueOf(eveAsset.getLocationID()));
			node.setAttributeNS(null, "singleton", String.valueOf(eveAsset.isSingleton()));
			parentNode.appendChild(node);
			writeAssets(xmldoc, node, eveAsset.getAssets());
		}
	}

	private static void writeAccountBalances(Document xmldoc, Element parentNode, List<ApiAccountBalance> accountBalances, boolean bCorp){
		Element node = xmldoc.createElementNS(null, "balances");
		if (!accountBalances.isEmpty()){
			node.setAttributeNS(null, "corp", String.valueOf(bCorp));
			parentNode.appendChild(node);
		}
		for (int a = 0; a < accountBalances.size(); a++){
			ApiAccountBalance accountBalance = accountBalances.get(a);

			Element childNode = xmldoc.createElementNS(null, "balance");
			childNode.setAttributeNS(null, "accountid", String.valueOf(accountBalance.getAccountID()));
			childNode.setAttributeNS(null, "accountkey", String.valueOf(accountBalance.getAccountKey()));
			childNode.setAttributeNS(null, "balance", String.valueOf(accountBalance.getBalance()));
			node.appendChild(childNode);
		}
	}
}
