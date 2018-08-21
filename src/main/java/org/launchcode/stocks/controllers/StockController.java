package org.launchcode.stocks.controllers;

import org.launchcode.stocks.models.Stock;
import org.launchcode.stocks.models.StockHolding;
import org.launchcode.stocks.models.StockLookupException;
import org.launchcode.stocks.models.User;
import org.launchcode.stocks.models.dao.StockHoldingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import yahoofinance.YahooFinance;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Chris Bay on 5/17/15.
 */
@Controller
public class StockController extends AbstractController {

    @Autowired
    StockHoldingDao stockHoldingDao;

    @RequestMapping(value = "/quote", method = RequestMethod.GET)
    public String quoteForm(Model model) {

        // pass data to template
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");
        return "quote_form";
    }

    @RequestMapping(value = "/quote", method = RequestMethod.POST)
    public String quote(String symbol, Model model) {
//
        Stock stock;
        try {
            stock = Stock.lookupStock(symbol);
        }
        catch (Exception e){
            return "quote_form";
        }
        model.addAttribute("stock_desc", stock.getName());
        model.addAttribute("stock_price", stock.getPrice());
        // pass data to template
        model.addAttribute("title", "Quote");
        model.addAttribute("quoteNavClass", "active");

        return "quote_display";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.GET)
    public String buyForm(Model model) {

        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public String buy(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

        // TODO - Implement buy action
        int userId = (int)request.getSession().getAttribute(userSessionKey);
        User user = userDao.findByUid(userId);
        StockHolding stockHolding;
        model.addAttribute("title", "Buy");
        model.addAttribute("action", "/buy");
        model.addAttribute("buyNavClass", "active");
        try {
            float moneySpent = Stock.lookupStock(symbol).getPrice() * numberOfShares;
            if(moneySpent > user.getCash()){
                throw new Exception("Insufficient funds biatchh");
            }
            stockHolding = StockHolding.buyShares(user, symbol, numberOfShares);
            user.setCash(user.getCash() - moneySpent);
            //System.out.println("SHAREEEES "+stockHolding.getSharesOwned());
            stockHoldingDao.save(stockHolding);
           // stockHoldingDao.findBySymbolAndOwnerId(symbol, userId);
            model.addAttribute("confirmMessage", "Owning shares: " + stockHolding.getSharesOwned() +"cash left: " + user.getCash());


        } catch (Exception e){System.out.println("EXCEPTIOOON: " + e.getMessage()); return "transaction_form";}
        return "transaction_confirm";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.GET)
    public String sellForm(Model model) {
        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");
        return "transaction_form";
    }

    @RequestMapping(value = "/sell", method = RequestMethod.POST)
    public String sell(String symbol, int numberOfShares, HttpServletRequest request, Model model) {

        // TODO - Implement sell action
        int userId = (int)request.getSession().getAttribute(userSessionKey);
        User user = userDao.findByUid(userId);
        StockHolding stockHolding;
        model.addAttribute("title", "Sell");
        model.addAttribute("action", "/sell");
        model.addAttribute("sellNavClass", "active");
        try {
            stockHolding = StockHolding.sellShares(user, symbol, numberOfShares);

           // System.out.println("SHAREEEES "+stockHolding.getSharesOwned());
            float moneyGain = Stock.lookupStock(symbol).getPrice() * numberOfShares;
            user.setCash(user.getCash() + moneyGain);
            stockHoldingDao.save(stockHolding);
            //stockHoldingDao.findBySymbolAndOwnerId(symbol, userId);
            model.addAttribute("confirmMessage", "Owning shares: " + stockHolding.getSharesOwned()+"cash left: "+user.getCash());


        } catch (Exception e){System.out.println("EXCEPTIOOON: " + e.getMessage()); return "transaction_form";}

        return "transaction_confirm";
    }

}
