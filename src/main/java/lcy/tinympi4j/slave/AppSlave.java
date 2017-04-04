package lcy.tinympi4j.slave;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

import jodd.http.HttpRequest;
import jodd.util.ClassLoaderUtil;
import jodd.util.SystemUtil;
import lcy.tinympi4j.common.SplitableTask;

public class AppSlave {
	
	private static final Logger logger = Logger.getLogger(AppSlave.class.getName()); 
	
	public static void main(String[] args) throws ServletException, LifecycleException {

		if (args.length == 0) {
			System.out.println("tinympi4j - a micro java offline distributed computation framework\nUSAGE:\n\tjava -jar tinympi4j-slave-0.2.jar {port}\nAUTHOR:\n\t林春宇@深圳\n\tchunyu_lin@163.com\n\thttps://github.com/binaryer/tinympi4j-slave");
			return;
		}

		//fixed slow SessionIdGeneratorBase.createSecureRandom
		if(SystemUtil.isHostLinux())
			System.setProperty("java.security.egd", "file:/dev/./urandom");
		
		
		final Tomcat tomcat = new Tomcat();
		tomcat.setPort(Integer.valueOf(args[0]));
		tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));  

		Context ctx = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
		tomcat.getConnector().setAttribute("maxThreads", 30);
		tomcat.getConnector().setAttribute("acceptCount", 10);
		tomcat.getConnector().setAttribute("connectionTimeout", 5000);
		tomcat.getConnector().setAttribute("minSpareThreads", 2);
		tomcat.getConnector().setAttribute("maxSpareThreads", 5);

		{
			Tomcat.addServlet(ctx, "hello", new HttpServlet() {

				private static final long serialVersionUID = 396928347122641219L;

				@Override
				protected void doGet(HttpServletRequest req, HttpServletResponse resp)
						throws ServletException, IOException {

					final Writer w = resp.getWriter();
					w.write("Hello, World!\n");
					w.flush();
					IOUtils.closeQuietly(w);
				}
			});
			ctx.addServletMappingDecoded("/hello", "hello");
		}
		{
			Tomcat.addServlet(ctx, "addtask", new HttpServlet() {
				private static final long serialVersionUID = -4699606734599776678L;

				@Override
				protected void doPut(final HttpServletRequest req, HttpServletResponse resp)
						throws ServletException, IOException {

					final String masterurl = req.getHeader("masterurl");
					final String slavetaskid = req.getHeader("slavetaskid");
					
					
					logger.info(String.format("received subtask, id = %s", slavetaskid));

					final byte[] bs = IOUtils.toByteArray(req.getInputStream());
					final Integer slaveto = Integer.valueOf(req.getHeader("slaveto"));
					
					final Serializable[] params = (Serializable[])SerializationUtils.deserialize(bs);
					
					Class<SplitableTask> clazz = null;
					try {
						clazz = ClassLoaderUtil.loadClass(req.getHeader("classname"));
					} catch (ClassNotFoundException e2) {
						clazz = ClassLoaderUtil.defineClass(req.getHeader("classname"), jodd.util.Base64.decode(req.getHeader("classbytes")));
					}
					
					final SplitableTask task = SplitableTaskFactory.instance(clazz);
					
					final Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							final Thread t = new Thread(new Runnable() {

								@Override
								public void run() {
									try {
										
										HttpRequest.put(masterurl+"/ok")
												.header("slavetaskid", slavetaskid)
												.body(SerializationUtils.serialize((Serializable) task.execute(params)), "TINYMPI4J-RETURN")
												.connectionTimeout(4000).send();
										logger.info(String.format("subtask %s finished", slavetaskid));
									} catch (Throwable e) {
										e.printStackTrace();
									}
									
								}

							});
							t.setDaemon(true);
							t.start();

							if (slaveto != null && slaveto > 0) {
								try {
									t.join(slaveto * 1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								if (t.isAlive()) {
									t.interrupt();
									try {
										TimeUnit.SECONDS.sleep(5L);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									if (t.isAlive()) {
										t.stop();
									}
								}
							} else {
								try {
									t.join();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}

					});
					t.setDaemon(true);
					t.start();

					final Writer w = resp.getWriter();
					w.write("ok\n");
					w.flush();
					IOUtils.closeQuietly(w);

				}
			});
			ctx.addServletMappingDecoded("/addtask", "addtask");
		}

		try {
			tomcat.start();
		} catch (Throwable t) {
			System.err.println(t.getMessage());
			System.exit(-1);
			return;
		}
		
		logger.info(String.format("slave started at port %d", Integer.valueOf(args[0])));
		tomcat.getServer().await();
	}

}
