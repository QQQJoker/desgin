package cn.sunline.clwj.zdbank.fmq.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final Pattern KVP_PATTERN = Pattern.compile("([_.a-zA-Z0-9][-_.a-zA-Z0-9]*)[=](.*)");
	private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

	public static boolean isBlank(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}

	public static boolean isEquals(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return true;
		} else {
			return s1 != null && s2 != null ? s1.equals(s2) : false;
		}
	}

	public static boolean isInteger(String str) {
		return str != null && str.length() != 0 ? INT_PATTERN.matcher(str).matches() : false;
	}

	public static int parseInteger(String str) {
		return !isInteger(str) ? 0 : Integer.parseInt(str);
	}

	public static boolean isJavaIdentifier(String s) {
		if (s.length() != 0 && Character.isJavaIdentifierStart(s.charAt(0))) {
			for (int i = 1; i < s.length(); ++i) {
				if (!Character.isJavaIdentifierPart(s.charAt(i))) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	public static boolean isEmpty(Object obj) {
		return isEmptyOrBlank(obj, false);
	}

	public static boolean isNotEmpty(Object obj) {
		return !isEmptyOrBlank(obj, false);
	}

	private static boolean isEmptyOrBlank(Object obj, boolean trim) {
		if (obj == null) {
			return true;
		} else if (obj instanceof String) {
			String ss = (String) obj;
			return (trim ? ss.trim() : ss).length() == 0;
		} else if (obj instanceof Object[]) {
			Object[] oo = (Object[]) ((Object[]) obj);

			for (int i = 0; i < oo.length; ++i) {
				if (!isEmptyOrBlank(oo[i], trim)) {
					return false;
				}
			}

			return true;
		} else if (obj instanceof Collection) {
			Collection<Object> oo = (Collection) obj;
			Iterator i = oo.iterator();

			do {
				if (!i.hasNext()) {
					return true;
				}
			} while (isEmptyOrBlank(i.next(), trim));

			return false;
		} else {
			return obj instanceof Map ? ((Map) obj).isEmpty() : false;
		}
	}

	public static boolean isContains(String values, String value) {
		return values != null && values.length() != 0 ? isContains(Pattern.compile("\\s*[,]+\\s*").split(values), value)
				: false;
	}

	public static boolean isContains(String[] values, String value) {
		if (value != null && value.length() > 0 && values != null && values.length > 0) {
			String[] var2 = values;
			int var3 = values.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				String v = var2[var4];
				if (value.equals(v)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		} else {
			int sz = str.length();

			for (int i = 0; i < sz; ++i) {
				if (!Character.isDigit(str.charAt(i))) {
					return false;
				}
			}

			return true;
		}
	}

	public static String translat(String src, String from, String to) {
		if (isEmpty(src)) {
			return src;
		} else {
			StringBuilder sb = null;
			int i = 0;

			for (int len = src.length(); i < len; ++i) {
				char c = src.charAt(i);
				int ix = from.indexOf(c);
				if (ix == -1) {
					if (sb != null) {
						sb.append(c);
					}
				} else {
					if (sb == null) {
						sb = new StringBuilder(len);
						sb.append(src, 0, i);
					}

					if (ix < to.length()) {
						sb.append(to.charAt(ix));
					}
				}
			}

			return sb == null ? src : sb.toString();
		}
	}

	public static String[] split(String str, char ch) {
		List<String> list = null;
		int ix = 0;
		int len = str.length();

		for (int i = 0; i < len; ++i) {
			char c = str.charAt(i);
			if (c == ch) {
				if (list == null) {
					list = new ArrayList();
				}

				list.add(str.substring(ix, i));
				ix = i + 1;
			}
		}

		if (ix > 0) {
			list.add(str.substring(ix));
		}

		return list == null ? EMPTY_STRING_ARRAY : (String[]) ((String[]) list.toArray(EMPTY_STRING_ARRAY));
	}

	public static String join(String[] array) {
		if (array.length == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			String[] var2 = array;
			int var3 = array.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				String s = var2[var4];
				sb.append(s);
			}

			return sb.toString();
		}
	}

	public static String join(String[] array, char split) {
		if (array.length == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < array.length; ++i) {
				if (i > 0) {
					sb.append(split);
				}

				sb.append(array[i]);
			}

			return sb.toString();
		}
	}

	public static String join(String[] array, String split) {
		if (array.length == 0) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < array.length; ++i) {
				if (i > 0) {
					sb.append(split);
				}

				sb.append(array[i]);
			}

			return sb.toString();
		}
	}

	public static String join(Collection<String> coll, String split) {
		if (coll.isEmpty()) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;

			String s;
			for (Iterator var4 = coll.iterator(); var4.hasNext(); sb.append(s)) {
				s = (String) var4.next();
				if (isFirst) {
					isFirst = false;
				} else {
					sb.append(split);
				}
			}

			return sb.toString();
		}
	}

	private static Map<String, String> parseKeyValuePair(String str, String itemSeparator) {
		String[] tmp = str.split(itemSeparator);
		Map<String, String> map = new HashMap(tmp.length);

		for (int i = 0; i < tmp.length; ++i) {
			Matcher matcher = KVP_PATTERN.matcher(tmp[i]);
			if (matcher.matches()) {
				map.put(matcher.group(1), matcher.group(2));
			}
		}

		return map;
	}

	public static String getQueryStringValue(String qs, String key) {
		Map<String, String> map = parseQueryString(qs);
		return (String) map.get(key);
	}

	public static Map<String, String> parseQueryString(String qs) {
		return (Map) (qs != null && qs.length() != 0 ? parseKeyValuePair(qs, "\\&") : new HashMap());
	}

	public static String getServiceKey(Map<String, String> ps) {
		StringBuilder buf = new StringBuilder();
		String group = (String) ps.get("group");
		if (group != null && group.length() > 0) {
			buf.append(group).append("/");
		}

		buf.append((String) ps.get("interface"));
		String version = (String) ps.get("version");
		if (version != null && version.length() > 0) {
			buf.append(":").append(version);
		}

		return buf.toString();
	}

	public static String toQueryString(Map<String, String> ps) {
		StringBuilder buf = new StringBuilder();
		if (ps != null && ps.size() > 0) {
			Iterator var2 = (new TreeMap(ps)).entrySet().iterator();

			while (var2.hasNext()) {
				Entry<String, String> entry = (Entry) var2.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				if (key != null && key.length() > 0 && value != null && value.length() > 0) {
					if (buf.length() > 0) {
						buf.append("&");
					}

					buf.append(key);
					buf.append("=");
					buf.append(value);
				}
			}
		}

		return buf.toString();
	}

	public static String camelToSplitName(String camelName, String split) {
		if (camelName != null && camelName.length() != 0) {
			StringBuilder buf = null;

			for (int i = 0; i < camelName.length(); ++i) {
				char ch = camelName.charAt(i);
				if (ch >= 'A' && ch <= 'Z') {
					if (buf == null) {
						buf = new StringBuilder();
						if (i > 0) {
							buf.append(camelName.substring(0, i));
						}
					}

					if (i > 0) {
						buf.append(split);
					}

					buf.append(Character.toLowerCase(ch));
				} else if (buf != null) {
					buf.append(ch);
				}
			}

			return buf == null ? camelName : buf.toString();
		} else {
			return camelName;
		}
	}
}
