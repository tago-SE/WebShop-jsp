package view.controllers;

import model.handlers.ShoppingHandler;
import model.handlers.exceptions.DatabaseException;
import model.handlers.exceptions.InvalidRequestException;
import model.handlers.exceptions.OutOfStockException;
import utils.Converter;
import view.viewmodels.Cart;
import view.viewmodels.Order;
import view.viewmodels.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import static view.Commands.*;
import static view.Pages.*;

@WebServlet(name = "Shopping")
public class ShoppingServlet extends BasicServlet {

    public static final String ORDER_MIN_ONE_ITEM_MSG = "Your cart is empty.";
    public static final String ORDER_SENT_MSG = "Order has been sent.";
    public static final String OUT_OF_STOCK_MSG = "Delivery cannot be made due to one or more items being out of stock.";
    public static final String DELIVERY_ALREADY_MADE_MSG = "Delivery has already been made before.";

    private static ShoppingHandler shoppingHandler = ShoppingHandler.getInstance();

    private void addToCart(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int itemId = Integer.parseInt(request.getParameter(ITEM_ID_ARG));
        User user = (User) session.getAttribute(ARG_CURR_USER);
        try {
            shoppingHandler.addItemToUserCart(user.getId(), itemId, 1);
            ShoppingHandler.Cart cart = shoppingHandler.getUserShoppingCart(user.getId());
            Cart cartView = Converter.toCart(cart);
            session.setAttribute(CART_ARG, cartView);
            response.sendRedirect(SHOP_JSP);
        } catch (Exception e) {
            errorResponse(request, response, UNKNOWN_EXCEPTION_MSG, SHOP_JSP);
            e.printStackTrace();
        }
    }

    private void dropCartItem(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int itemId = Integer.parseInt(request.getParameter(ITEM_ID_ARG));
        User user = (User) session.getAttribute(ARG_CURR_USER);
        try {
            shoppingHandler.removeFromUserCart(user.getId(),itemId);
            ShoppingHandler.Cart cart = shoppingHandler.getUserShoppingCart(user.getId());
            Cart cartView = Converter.toCart(cart);
            session.setAttribute(CART_ARG, cartView);
            response.sendRedirect(SHOPPING_CART_JSP);
        } catch (Exception e) {
            errorResponse(request, response, UNKNOWN_EXCEPTION_MSG, SHOPPING_CART_JSP);
            e.printStackTrace();
        }
    }

    private void editCartItem(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) session.getAttribute(ARG_CURR_USER);
        try {
            int itemId = Integer.parseInt(request.getParameter(ITEM_ID_ARG));
            int amount = Integer.parseInt(request.getParameter(ITEM_AMOUNT_ARG));
            shoppingHandler.setItemInUserCart(user.getId(), itemId, amount);
            Cart cartView = Converter.toCart(shoppingHandler.getUserShoppingCart(user.getId()));
            session.setAttribute(CART_ARG, cartView);
            response.sendRedirect(SHOPPING_CART_JSP);
        } catch (Exception e) {
            e.printStackTrace();
            errorResponse(request, response, UNKNOWN_EXCEPTION_MSG, SHOPPING_CART_JSP);
        }
    }

    private void makeOrder(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) session.getAttribute(ARG_CURR_USER);
        switch (shoppingHandler.placeOrder(user.getId())) {
            case ShoppingHandler.ORDER_OK:
                Cart cartView = Converter.toCart(shoppingHandler.getUserShoppingCart(user.getId()));
                session.setAttribute(CART_ARG, cartView);
                successResponse(request, response, ORDER_SENT_MSG, SHOPPING_CART_JSP);
                break;
            case ShoppingHandler.ORDER_FAIL:
                errorResponse(request, response, ORDER_MIN_ONE_ITEM_MSG, SHOPPING_CART_JSP);
                break;
            default:
                errorResponse(request, response, UNKNOWN_EXCEPTION_MSG, SHOPPING_CART_JSP);
        }
    }

    private void getAllOrders(HttpSession session, HttpServletRequest request, HttpServletResponse response, List<String> access) throws IOException, ServletException {
        String redirect = request.getParameter(REDIRECT_ARG);
        String sortByDelivered = request.getParameter(DELIVERED_SORT_ARG);
        String sortByUndelivered = request.getParameter(UNDELIVERED_SORT_ARG);
        try {
            shoppingHandler.getAllOrders(access);
            List<Order> orders = shoppingHandler.getAllOrders(access);
            if (sortByDelivered != null && sortByDelivered.equals("1")) {
                // Not yet implemented
            }
            else if (sortByUndelivered != null && sortByUndelivered.equals("1")) {
                // Not yet implemented
            }
            session.setAttribute(ORDERS_ARG, orders);
            response.sendRedirect(redirect);
        } catch (IllegalAccessException e) {
            errorResponse(request,response, ILLEGAL_ACCESS_MSG, redirect);
        } catch (DatabaseException e) {
            e.printStackTrace();
            errorResponse(request, response, DB_EXCEPTION_MSG, redirect);
        }
    }

    private void deliverOrder(HttpSession session, HttpServletRequest request, HttpServletResponse response, List<String> access) throws IOException, ServletException {
        int orderId = Integer.parseInt(request.getParameter(ORDER_ID_ARG));
        String redirect = request.getParameter(REDIRECT_ARG);
        try {
            shoppingHandler.deliverOrder(orderId);
            request.setAttribute(REDIRECT_ARG, redirect);
            getAllOrders(session, request, response, access);
        } catch (Exception e) {
            if (e instanceof DatabaseException) {
                e.printStackTrace();
                errorResponse(request,response, DB_EXCEPTION_MSG, redirect);
            }
            else if (e instanceof OutOfStockException) {
                errorResponse(request,response, OUT_OF_STOCK_MSG, redirect);
            }
            else if (e instanceof InvalidRequestException) {
                errorResponse(request,response, DELIVERY_ALREADY_MADE_MSG, redirect);
            } else {
                e.printStackTrace();
                errorResponse(request,response, UNKNOWN_EXCEPTION_MSG, redirect);
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String command = translateRequestToCommand(request);
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(ARG_CURR_USER);
        List<String> access = (List<String>) user.getAccessRoles();
        if (user == null) {
            return;
        }
        if (command != null && command.length() > 0) {
            switch (command) {
                case ADD_TO_CART_CMD: addToCart(session, request, response); break;
                case GOTO_CART_CMD:
                case GOTO_CMD: gotoPage(session, request, response); break;
                case CUSTOMER_ORDER_CMD: makeOrder(session, request, response); break;
                case REMOVE_FROM_CART_CMD: dropCartItem(session, request, response); break;
                case EDIT_CART_ITEM_ARG: editCartItem(session, request, response); break;
                case GET_ALL_ORDERS_CMD: getAllOrders(session, request, response, access); break;
                case DELIVER_ORDER_CMD: deliverOrder(session, request, response, access); break;
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
