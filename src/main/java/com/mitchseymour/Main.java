package com.mitchseymour;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.*;

/**
 * Program entry point. Starts up and runs the servlet. Defines the servlet end points and delegate
 * request processing.
 *
 * <p>AVOID CHANGING ANYTHING IN THIS FILE. If you do make changes please provide justification.
 *
 * <p>Note: I made a couple of minor changes to this file to improve logging and threadpool
 * construction
 */
public class Main {

  static final Logger log = LoggerFactory.getLogger(Main.class);
  /**
   * Initializes and runs the jetty servlet.
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    // be explicit with threadpool
    // justification: more fine-grained control over concurrency
    QueuedThreadPool threadPool =
        new QueuedThreadPool(
            100, // max threads
            10 // min threads
            );
    Server server = new Server(threadPool);

    ServerConnector http = new ServerConnector(server);
    http.setPort(8080);
    server.setConnectors(new Connector[] {http});

    HandlerCollection handlers = new HandlerCollection();
    ServletHandler servletHandler = new ServletHandler();
    servletHandler.addServletWithMapping(SimCalcServlet.class, "/*");
    handlers.addHandler(servletHandler);

    // add request logging
    // justification: better observability
    RequestLogHandler reqLogs = new RequestLogHandler();
    Slf4jRequestLog requestLog = new Slf4jRequestLog();
    requestLog.setLoggerName("access-log");
    requestLog.setLogLatency(true);
    reqLogs.setRequestLog(requestLog);
    handlers.addHandler(reqLogs);

    // add all the handlers
    server.setHandler(handlers);

    server.start();

    // The use of server.join() the will make the current thread join and
    // wait until the server is done executing.
    server.join();
  }

  /**
   * Servlet definition. Implements handlers for the supported http methods. The endpoints are:
   *
   * <p>GET /termFrequencies documentText="url encoded text of document"
   *
   * <p>/similarityScore documentText1="url encoded text of first document" documentText2="url
   * encoded text of second document"
   *
   * <p>/genreDocuments genre="name of genre"
   *
   * <p>/nClosestGenres documentText="url encoded text of document" count="maximum number of genres
   * to return in response"
   *
   * <p>PUT /genreDocument genre="name of genre" docId="id of document" documentText="url encoded
   * text of document"
   *
   * <p>DELETE /genreDocument genre="name of genre" docId="id of document"
   */
  @SuppressWarnings("serial")
  public static class SimCalcServlet extends HttpServlet {
    private RequestHandler requestHandler = new RequestHandler();

    /** Handle http get. Dispatch the handling of requests for the supported rest end points */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      try {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        if (request.getPathInfo().equals("/termFrequencies")) {
          String docText = request.getParameter("documentText");
          Map<CharSequence, Double> termFrequencies = requestHandler.getTermFrequencies(docText);
          String responseBody = doubleMapToJson(termFrequencies);
          response.getWriter().println(responseBody);
        } else if (request.getPathInfo().equals("/similarityScore")) {
          String doc1Text = request.getParameter("documentText1");
          String doc2Text = request.getParameter("documentText2");
          Double similarityScore = requestHandler.getSimilarityScore(doc1Text, doc2Text);
          response.getWriter().println(similarityScore.toString());
        } else if (request.getPathInfo().equals("/genreDocuments")) {
          String genre = request.getParameter("genre");
          List<String> docIds = requestHandler.getDocumentsInGenre(genre);
          String responseBody = stringListToJson(docIds);
          response.getWriter().println(responseBody);
        } else if (request.getPathInfo().equals("/nClosestGenres")) {
          String documentText = request.getParameter("documentText");
          String count = request.getParameter("count");

          List<String> genres =
              requestHandler.getNClosestGenres(documentText, Integer.parseInt(count));
          String responseBody = stringListToJson(genres);
          response.getWriter().println(responseBody);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
      } catch (Exception e) {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println(e.toString());
      }
    }

    /** Handle http put. Dispatch the handling of requests for the supported rest end points */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      try {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        if (request.getPathInfo().equals("/genreDocument")) {
          String genre = request.getParameter("genre");
          String docId = request.getParameter("docId");
          String documentText = request.getParameter("documentText");
          requestHandler.addDocumentToGenre(genre, docId, documentText);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
      } catch (Exception e) {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println(e.toString());
      }
    }

    /** Handle http delete. Dispatch the handling of requests for the supported rest end points */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      try {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        if (request.getPathInfo().equals("/genreDocument")) {
          String genre = request.getParameter("genre");
          String docId = request.getParameter("docId");
          requestHandler.removeDocumentFromGenre(genre, docId);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
      } catch (Exception e) {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println(e.toString());
      }
    }

    /** Serialize a Map<CharSequence,Double> to json: '{key1:value1, key2:value2, ...}' */
    private String doubleMapToJson(Map<CharSequence, Double> map) {
      StringBuilder result = new StringBuilder();
      result.append('{');

      map.forEach(
          (k, v) -> {
            result.append('"').append(k).append("\":").append(v.intValue()).append(',');
          });

      if (result.length() > 1) {
        result.deleteCharAt(result.length() - 1); // remove trailing comma
      }

      result.append('}');

      return result.toString();
    }

    /**
     * Serialize a List<String> to json: '["item1","item2","item3"]'
     *
     * @param list
     * @return
     */
    private String stringListToJson(List<String> list) {
      StringBuilder result = new StringBuilder();
      result.append('[');

      list.forEach(
          (item) -> {
            result.append('"').append(item).append("\",");
          });

      if (result.length() > 1) {
        result.deleteCharAt(result.length() - 1); // remove trailing comma
      }

      result.append(']');

      return result.toString();
    }
  }
}
