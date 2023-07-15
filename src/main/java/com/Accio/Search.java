package com.Accio;

import com.Accio.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet("/Search")
public class Search extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Getting keyword from the front end
        String keyword = request.getParameter("keyword");

        // Getting connection to the database
        Connection connection = DatabaseConnection.getConnection();

        try {
            // Store the query of the user
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO history VALUES (?, ?);");
            preparedStatement.setString(1, keyword);
            preparedStatement.setString(2, "http://localhost:8080/SearchEngine/Search?keyword=" + keyword);
            preparedStatement.executeUpdate();

            // Getting result after running the ranking query
            String searchQuery = "SELECT pageTitle, pageLink, (length(lower(pageText)) - length(replace(lower(pageText), ?, ''))) / length(?) AS countoccurance " +
                    "FROM pages " +
                    "ORDER BY countoccurance DESC " +
                    "LIMIT 30;";
            preparedStatement = connection.prepareStatement(searchQuery);
            preparedStatement.setString(1, keyword.toLowerCase());
            preparedStatement.setString(2, keyword.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();

            ArrayList<SearchResult> results = new ArrayList<>();
            // We are transferring values from resultSet to the results ArrayList
            while (resultSet.next()) {
                SearchResult searchResult = new SearchResult();
                searchResult.setTitle(resultSet.getString("pageTitle"));
                searchResult.setLink(resultSet.getString("pageLink"));
                results.add(searchResult);
            }

            // Displaying the result ArrayList in the console
            for (SearchResult result : results) {
                System.out.println(result.getTitle() + "\n" + result.getLink() + "\n");
            }

            request.setAttribute("results", results);
            request.getRequestDispatcher("search.jsp").forward(request, response);

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
        } catch (SQLException | ServletException sqlException) {
            sqlException.printStackTrace();
        }
    }
}