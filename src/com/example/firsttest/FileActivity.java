package com.example.firsttest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class FileActivity extends ListActivity {
	private static final String ROOT_PATH = "/";
	private static File fromFile;
	private static int position1;
	private static String path = null;
	// 存储文件名称
	private ArrayList<String> names = null;
	// 存储文件路径
	private ArrayList<String> paths = null;
	private View view;
	private EditText editText;
	private EditText editText1;
	private File lastfile;
	private static final int ITEM_COPY = 1;
	private static final int ITEM_PASTE = 2;
	private ProgressDialog dialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.filemanage);
		dialog = ProgressDialog.show(FileActivity.this, "加载中...",
				"正在载入数据......", true);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 显示文件列表
				showFileDir(ROOT_PATH);
				
				String apkRoot = "chmod 777 " + getPackageCodePath();
				SystemManager.RootCommand(apkRoot);
				handler.sendEmptyMessage(0);// 执行耗时的方法之后发送消给handler
			}
		}).start();
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// handler接收到消息后就会执行此方法
			registerForContextMenu(getListView());// 设置长按菜单
			Button fileBack = (Button) findViewById(R.id.file_back);
			Button fileEdit = (Button) findViewById(R.id.file_add);
			fileBack.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (lastfile.getParent() != null){
						showFileDir(lastfile.getParent());
						getListView().setSelection(position1);//
					}
					else
						FileActivity.this.finish();

				}
			});
			fileEdit.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (lastfile.canRead() && lastfile.canWrite())
						fileaddHandle();
					else {
						Resources res = getResources();
						new AlertDialog.Builder(FileActivity.this)
								.setTitle("Message")
								.setMessage(res.getString(R.string.no_permission))
								.setPositiveButton("OK", new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).show();
					}
				}
			});
			
			getListView().setOnScrollListener(new OnScrollListener() {  
				
				/** 
				 * 滚动状态改变时调用 
				 */  
				@Override  
				public void onScrollStateChanged(AbsListView view, int scrollState) {  
					// 不滚动时保存当前滚动到的位置  
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {  
						position1 = getListView().getFirstVisiblePosition();  
					}  
				}  
				
				/** 
				 * 滚动时调用 
				 */  
				@Override  
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {  
				}  
			});
			dialog.dismiss();// 关闭ProgressDialog
		}
	};

	private void showFileDir(String path) {
		names = new ArrayList<String>();
		paths = new ArrayList<String>();
		File file = new File(path);
		lastfile = file;
		File[] files = file.listFiles();

		// 如果当前目录不是根目录
		if (!ROOT_PATH.equals(path)) {
			names.add("@1");
			paths.add(ROOT_PATH);

			names.add("@2");
			paths.add(file.getParent());
		}
		// 添加所有文件
		for (File f : files) {
			names.add(f.getName());
			paths.add(f.getPath());
		}
		this.setListAdapter(new FileAdapter(this, names, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String path = paths.get(position);
		File file = new File(path);
		// 文件存在并可读
		if (file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				// 显示子目录及文件
				showFileDir(path);
			} else {
				// 处理文件
				fileHandle(file);
				//getListView().setSelection(position1); // 
			}
		}
		// 没有权限
		else {
			Resources res = getResources();
			new AlertDialog.Builder(this).setTitle("Message")
					.setMessage(res.getString(R.string.no_permission))
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
		}
		super.onListItemClick(l, v, position, id);
	}

	// 对文件进行增删改
	private void fileHandle(final File file) {
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 打开文件
				if (which == 0) {
					openFile(file);
				}
				// 修改文件名
				else if (which == 1) {
					LayoutInflater factory = LayoutInflater
							.from(FileActivity.this);
					view = factory.inflate(R.layout.rename_dialog, null);
					editText = (EditText) view.findViewById(R.id.renameText);
					editText.setText(file.getName());

					OnClickListener listener2 = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String modifyName = editText.getText().toString();
							final String fpath = file.getParentFile().getPath();
							final File newFile = new File(fpath + "/"
									+ modifyName);
							if (newFile.exists()) {
								// 排除没有修改情况
								if (!modifyName.equals(file.getName())) {
									new AlertDialog.Builder(FileActivity.this)
											.setTitle("注意!")
											.setMessage("文件名已存在，是否覆盖？")
											.setPositiveButton(
													"确定",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															if (file.renameTo(newFile)) {
																showFileDir(fpath);
																displayToast("重命名成功！");
															} else {
																displayToast("重命名失败！");
															}
														}
													})
											.setNegativeButton(
													"取消",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {

														}
													}).show();
								}
							} else {
								if (file.renameTo(newFile)) {
									showFileDir(fpath);
									displayToast("重命名成功！");
								} else {
									displayToast("重命名失败！");
								}
							}
						}
					};
					AlertDialog renameDialog = new AlertDialog.Builder(
							FileActivity.this).create();
					renameDialog.setView(view);
					renameDialog.setButton("确定", listener2);
					renameDialog.setButton2("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					renameDialog.show();
				}
				// 删除文件
				else {
					new AlertDialog.Builder(FileActivity.this)
							.setTitle("注意!")
							.setMessage("确定要删除此文件吗？")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (file.delete()) {
												// 更新文件列表
												showFileDir(file.getParent());
												displayToast("删除成功！");
											} else {
												displayToast("删除失败！");
											}
										}
									})
							.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									}).show();
				}
			}
		};
		// 选择文件时，弹出增删该操作选项对话框
		String[] menu = { "打开文件", "重命名", "删除文件" };
		new AlertDialog.Builder(FileActivity.this).setTitle("请选择要进行的操作")
				.setItems(menu, listener)
				.setPositiveButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}

	private void fileaddHandle() {
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 打开文件
				if (which == 0) {
					LayoutInflater factory = LayoutInflater
							.from(FileActivity.this);
					view = factory.inflate(R.layout.filemanage_add, null);
					editText1 = (EditText) view.findViewById(R.id.addText);
					OnClickListener listener3 = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String modifyName = editText1.getText().toString();
							final String fpath = lastfile.getPath();
							final File newFile = new File(fpath + "/"
									+ modifyName);
							if (newFile.exists()) {
								// 排除没有修改情况
								if (names.contains(modifyName)) {
									new AlertDialog.Builder(FileActivity.this)
											.setTitle("注意!")
											.setMessage("文件名已存在")
											.setPositiveButton(
													"确定",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {

														}
													}).show();
								}
							} else {
								if (newFile.mkdirs()) {
									showFileDir(fpath);
									displayToast("新建成功！");
								} else {
									displayToast("新建失败！");
								}
							}
						}
					};
					AlertDialog renameDialog = new AlertDialog.Builder(
							FileActivity.this).create();
					renameDialog.setView(view);
					renameDialog.setButton("确定", listener3);
					renameDialog.setButton2("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					renameDialog.show();
				}
				// 修改文件名
				else if (which == 1) {
					LayoutInflater factory = LayoutInflater
							.from(FileActivity.this);
					view = factory.inflate(R.layout.filemanage_add, null);
					editText1 = (EditText) view.findViewById(R.id.addText);
					OnClickListener listener3 = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String modifyName = editText1.getText().toString();
							final String fpath = lastfile.getPath();
							final File newFile = new File(fpath + "/"
									+ modifyName);
							if (newFile.exists()) {
								// 排除没有修改情况
								if (names.contains(modifyName)) {
									new AlertDialog.Builder(FileActivity.this)
											.setTitle("注意!")
											.setMessage("文件名已存在，是否覆盖？")
											.setPositiveButton(
													"确定",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															if (newFile
																	.canWrite()) {
																try {
																	FileOutputStream fs = new FileOutputStream(
																			newFile);
																	fs.close();
																} catch (FileNotFoundException e) {
																	// TODO
																	// Auto-generated
																	// catch
																	// block
																	e.printStackTrace();
																} catch (IOException e) {
																	// TODO
																	// Auto-generated
																	// catch
																	// block
																	e.printStackTrace();
																}
																showFileDir(fpath);
																displayToast("新建成功！");
															} else {
																displayToast("新建失败！");
															}
														}
													})
											.setNegativeButton(
													"取消",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {

														}
													}).show();
								}
							} else {
								try {
									if (newFile.createNewFile()) {
										showFileDir(fpath);
										displayToast("新建成功！");
									} else {
										displayToast("新建失败！");
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					};
					AlertDialog renameDialog = new AlertDialog.Builder(
							FileActivity.this).create();
					renameDialog.setView(view);
					renameDialog.setButton("确定", listener3);
					renameDialog.setButton2("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					renameDialog.show();
				}
			}
		};
		String[] menu = { "新建文件夹", "新建文件" };
		new AlertDialog.Builder(FileActivity.this).setTitle("请选择要进行的操作")
				.setItems(menu, listener)
				.setPositiveButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}

	// 打开文件
	private void openFile(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(file);
		intent.setDataAndType(Uri.fromFile(file), type);
		startActivity(intent);
	}

	// 获取文件mimetype
	private String getMIMEType(File file) {
		String type = "";
		String name = file.getName();
		// 文件扩展名
		String end = name.substring(name.lastIndexOf(".") + 1, name.length())
				.toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("mp4") || end.equals("3gp")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg")
				|| end.equals("bmp") || end.equals("gif")) {
			type = "image";
		} else {
			// 如果无法直接打开，跳出列表由用户选择
			type = "*";
		}
		type += "/*";
		return type;
	}

	private void displayToast(String message) {
		Toast.makeText(FileActivity.this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	// 处理长按
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		// super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("请选择操作");
		menu.add(0, ITEM_COPY, 0, "复制");
		menu.add(0, ITEM_PASTE, 0, "粘贴");

	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo(); // info.targetView得到list.xml中的LinearLayout对象.
		int Id = (int) info.position;
		if (item.getItemId() == ITEM_PASTE) {
			if (path != null) {
				if (fromFile.isFile()) {
					File toFile = new File(lastfile.toString() + "/"
							+ fromFile.getName());
					// System.out.println("111111" + toFile.toString());
					if (!names.contains(fromFile.getName())) {
						if (copyfile(fromFile, toFile, true)) {
							showFileDir(lastfile.getAbsolutePath());
							path = null;
							displayToast("粘贴成功");
						} else
							displayToast("粘贴失败");
					} else {
						new AlertDialog.Builder(FileActivity.this)
								.setTitle("注意!")
								.setMessage("文件已存在，是否覆盖？")
								.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												if (copyfile(
														fromFile,
														new File(
																lastfile.toString()
																		+ "/"
																		+ fromFile
																				.getName()),
														true)) {
													showFileDir(lastfile
															.getAbsolutePath());
													path = null;
													displayToast("粘贴成功");

												} else {
													displayToast("粘贴失败！");
												}
											}
										})
								.setNegativeButton("取消",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												path = null;
											}
										}).show();
					}
				} else {
					if (copyFolder(fromFile.toString(), lastfile.toString()
							+ File.separator + fromFile.getName())) {
						showFileDir(lastfile.getAbsolutePath());
						path = null;
						displayToast("粘贴成功");
					} else
						displayToast("粘贴失败");
				}
			} else
				displayToast("请先复制");
		}
		if (item.getItemId() == ITEM_COPY) {
			path = paths.get(Id);
			fromFile = new File(path);
		}
		return true;

	}

	public boolean copyfile(File fromFile, File toFile, Boolean rewrite) {
		if (!fromFile.exists()) {
			return false;
		}
		if (!fromFile.isFile()) {
			return false;
		}
		if (!fromFile.canRead()) {
			return false;
		}
		if (!toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}

		if (toFile.exists() && rewrite) {
			toFile.delete();
		}

		try {
			FileInputStream fosfrom = new java.io.FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c); // 将内容写到新文件当中
			}
			fosfrom.close();
			fosto.close();
		} catch (Exception ex) {
			Log.e("readfile", ex.getMessage());
			return false;
		}
		return true;
	}

	public boolean copyFolder(String oldPath, String newPath) {
		if (!new File(oldPath).exists()) {
			return false;
		}
		if (!new File(oldPath).isDirectory()) {
			return false;
		}
		if (!new File(oldPath).canRead()) {
			return false;
		}
		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			return false;

		}
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		path = null;
		super.onDestroy();
	}
}

class SystemManager {
	/**
	 * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
	 * 
	 * @param command
	 *            命令：String apkRoot="chmod 777 "+getPackageCodePath();
	 *            RootCommand(apkRoot);
	 * @return 应用程序是/否获取Root权限
	 */
	public static boolean RootCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		Log.d("*** DEBUG ***", "Root SUC ");
		return true;
	}
}
