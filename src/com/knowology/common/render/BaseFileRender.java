package com.knowology.common.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import com.jfinal.kit.PathKit;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import com.jfinal.render.RenderFactory;

import demo.DemoConfig;

/**
 * FileRender.
 */
public class BaseFileRender extends Render {

	private static final long serialVersionUID = 4293616220202691369L;
	private File file;
	private String fileName;
	private static String fileDownloadPath;
	private static String webRootPath;

	public BaseFileRender(File file) {
		this.file = file;
	}

	public BaseFileRender(String fileName) {
		this.fileName = fileName;
	}

	static {
		BaseFileRender.fileDownloadPath = DemoConfig.fileDirectory;
		webRootPath = PathKit.getWebRootPath();

	}

	public void render() {
		if (fileName != null) {
			if (fileName.startsWith("/"))
				file = new File(webRootPath + fileName);
			else
				file = new File(fileDownloadPath + fileName);
		}
		if (file == null || !file.isFile() || file.length() > Integer.MAX_VALUE) {
			// response.sendError(HttpServletResponse.SC_NOT_FOUND);
			// return;
			// throw new RenderException("File not found!");
			request.setAttribute("errorM", "当前请求的文件：" + file.getAbsolutePath()
					+ "不存在！");
			RenderFactory.me().getErrorRender(404)
					.setContext(request, response).render();
			return;
		}
		try {
			response.addHeader("Content-disposition", "attachment; filename="
					+ new String(file.getName().getBytes("GBK"), "ISO8859-1"));
		} catch (UnsupportedEncodingException e) {
			response.addHeader("Content-disposition", "attachment; filename="
					+ file.getName());
		}
		String contentType = "application/octet-stream";
		// if (contentType == null) {
		// contentType = DEFAULT_FILE_CONTENT_TYPE; //
		// "application/octet-stream";
		// }
		response.setContentType(contentType);
		response.setContentLength((int) file.length());
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
			outputStream = response.getOutputStream();
			byte[] buffer = new byte[1024];
			for (int n = -1; (n = inputStream.read(buffer)) != -1;) {
				outputStream.write(buffer, 0, n);
			}
			outputStream.flush();
		} catch (Exception e) {
			throw new RenderException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}