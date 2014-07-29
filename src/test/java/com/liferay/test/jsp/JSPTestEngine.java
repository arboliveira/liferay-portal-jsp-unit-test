
package com.liferay.test.jsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.runtime.TldScanner;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;

/**
 * Troubleshooting: java.lang.NoClassDefFoundError:
 * org/apache/tomcat/PeriodicEventListener Solution: Launch Configuration
 * Classpath User Entries 1ST: Maven Dependencies 2ND: Eclipse project
 */
public class JSPTestEngine {

	ServletTester tester = newServletTester();
	HttpTester request = new HttpTester();
	HttpTester response = new HttpTester();

	public interface HttpServletRequestPrepare {

		void prepare(HttpServletRequest request);

	}

	public JSPTestEngine(HttpServletRequestPrepare httpServletRequestPrepare) {
		ServletHolder servlet = addJspServlet(httpServletRequestPrepare);
		keepGenerated(servlet);

		request.setMethod("GET");
		request.setVersion("HTTP/1.0");
	}

	public void setResourceBase(String resourceBase) {
		this.tester.setResourceBase(resourceBase);
	}

	public void setURI(String uri) {
		this.request.setURI(uri);
	}

	public ServletTester getServletTester() {
		return tester;
	}

	public ResponseContent execute() throws Exception {
		HttpTester response = goTester();

		assertTrue(response.getMethod() == null);
		assertEquals(200, response.getStatus());

		String content = response.getContent();

		return new ResponseContent(content);
	}

	private HttpTester goTester() throws Exception {
		tester.start();
		response.parse(tester.getResponses(request.generate()));
		return response;
	}

	private static ServletTester newServletTester() {
		ServletTester tester = new ServletTester();
		workaroundMissingClassLoader(tester);
		return tester;
	}

	private static void workaroundMissingClassLoader(ServletTester tester) {
		tester.setClassLoader(tester.getClass().getClassLoader());
	}

	private static void keepGenerated(ServletHolder servlet) {
		servlet.setInitParameter("keepgenerated", "TRUE");
		servlet.setInitParameter("scratchdir", "./target/scratch");
	}

	private ServletHolder addJspServlet(
		HttpServletRequestPrepare httpServletRequestPrepare) {

		ServletHandler servletHandler = tester.getContext().getServletHandler();

		ViewJspServlet servlet = new ViewJspServlet(httpServletRequestPrepare);

		ServletHolder holder =
			servletHandler.newServletHolder(Holder.Source.EMBEDDED);
		holder.setServlet(servlet);

		servletHandler.addServletWithMapping(holder, "*.jsp");

		return holder;
	}

	static class ViewJspServlet extends JspServlet {

		private final HttpServletRequestPrepare httpServletRequestPrepare;

		public ViewJspServlet(
			HttpServletRequestPrepare httpServletRequestPrepare) {

			this.httpServletRequestPrepare = httpServletRequestPrepare;
		}

		@Override
		public void service(
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

			httpServletRequestPrepare.prepare(request);

			super.service(request, response);
		}

	}

	static {
		try {
			workaroundJettyTaglibJstlCoreSystemUris();
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static void workaroundJettyTaglibJstlCoreSystemUris()
		throws NoSuchFieldException, IllegalAccessException {
		Field field = TldScanner.class.getDeclaredField("systemUris");
		field.setAccessible(true);
		((Set<?>)field.get(null)).clear();
		field.setAccessible(false);
	}

}
