package org.biometric.provider;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CORSManager  {

    public static void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        //TODO: This has a security flaw. Keep a list of whitelisted domains and set headers to a whitelisted domain if the request is from a whitelisted domain.
        //response.setHeader("Access-Control-Allow-Origin", "https://foo.example");
        response.setHeader("Access-Control-Allow-Origin","*");
        PrintWriter out = response.getWriter();
        out.println("");
    }
}