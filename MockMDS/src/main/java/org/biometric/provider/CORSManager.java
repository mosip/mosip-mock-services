package org.biometric.provider;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

public class CORSManager  {

    @Value("${cors.headers.allowed.methods: OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM}")
    public static String allowedMethods;

    @Value("${cors.headers.allowed.origin: *}")
    public static String allowedOrigin;

    public static void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        //TODO: This has a security flaw. Keep a list of whitelisted domains and set headers to a whitelisted domain if the request is from a whitelisted domain.
        //response.setHeader("Access-Control-Allow-Origin", "https://foo.example");
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods","OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET");
        System.out.println("Set the CORS headers");
        PrintWriter out = response.getWriter();
        out.println("");
    }

    public static HttpServletResponse setCors(HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods","OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET");
        return response;
    }
}