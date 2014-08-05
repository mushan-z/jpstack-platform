package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import lombok.SneakyThrows;

public
class Html {

	public static
	void detailsTrText (
			PrintWriter out,
			String label,
			String name,
			int size,
			String value) {

		out.print (
			stringFormat (

				"<tr>\n",

				"<th>%h</th>",
				label,

				"<td><input",
				" type=\"text\"",
				" name=\"%h\"",
				name,
				" size=\"%h\"",
				size,
				" value=\"%h\"",
				value,
				"></td>\n",

				"</tr>\n"));

	}

	public static
	String encode (
			String source,
			String ifNull) {

		if (source == null)
			return ifNull;

		StringBuilder dest =
			new StringBuilder (source.length () * 2);

		for (
			int index = 0;
			index < source.length ();
			index ++
		) {

			char ch =
				source.charAt (index);

			switch (ch) {

			case '<':
				dest.append ("&lt;");
				break;

			case '>':
				dest.append ("&gt;");
				break;

			case '&':
				dest.append ("&amp;");
				break;

			case '"':
				dest.append ("&quot;");
				break;

			default:

				if (ch < 128)
					dest.append (ch);
				else
					dest.append ("&#" + (int) ch + ";");

			}

		}

		return dest.toString ();

	}

	public static
	String encode (
			String source) {

		return encode (
			source,
			"");

	}

	public static
	String encode (
			Object object) {

		return object == null
			? ""
			: encode (
				object.toString ());

	}

	public static
	String encode (
			Object object,
			String valueIfNull) {

		return object == null
			? valueIfNull
			: encode (
				object.toString ());

	}

	public static
	String encode (
			int intValue) {

		return encode (
			Integer.toString (
				intValue));

	}

	public static
	String javascriptStringEscape (
			String source) {

		if (source == null)
			return null;

		StringBuilder dest =
			new StringBuilder (
				source.length () * 2);

		for (
			int pos = 0;
			pos < source.length ();
			pos++
		) {

			char ch =
				source.charAt (pos);

			switch (ch) {

			case '"':
				dest.append ("\\\"");
				break;

			case '\'':
				dest.append ("\\'");
				break;

			case '\n':
				dest.append ("\\n");
				break;

			case '\r':
				dest.append ("\\r");
				break;

			case '/':
				dest.append ("\\/");
				break;

			default:
				dest.append (ch);

			}

		}

		return dest.toString ();

	}

	public static
	String jsqe (
			String string) {

		return encode (
			javascriptStringEscape (
				string));

	}

	public static
	String ensureSlash (
			String string) {

		if (string == null || string.equals (""))
			return "/";

		return string;

	}

	public static
	String magicTr (
			String href,
			boolean selected,
			String target,
			String extra,
			String style,
			ABSwap abSwap) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		// begin element

		stringBuilder.append ("<tr");

		// class attribute

		if (selected) {

			stringBuilder.append (" class=\"selected\"");

		} else if (abSwap != null) {

			stringBuilder.append (" class=\"");
			stringBuilder.append (abSwap.swap ());
			stringBuilder.append ("\"");

		}

		// style attribute

		stringBuilder.append (" style=\"cursor: pointer;");

		if (style != null) {

			stringBuilder.append (' ');
			stringBuilder.append (style);

		}

		stringBuilder.append ("\"");

		// onmouseover attribute

		stringBuilder.append (" onmouseover=\"this.className='hover'\"");

		// onmouseout attribute

		if (selected) {

			stringBuilder.append (" onmouseout=\"this.className='selected'\"");

		} else {

			stringBuilder.append (" onmouseout=\"this.className=null\"");

		}

		// onclick attribute

		if (href != null) {

			if (target != null) {

				stringBuilder.append (" onclick=\"top.frames['");
				stringBuilder.append (Html.jsqe (target));
				stringBuilder.append ("'].location='");
				stringBuilder.append (Html.jsqe (href));
				stringBuilder.append ("'\"");

			} else {

				stringBuilder.append (" onclick=\"window.location='");
				stringBuilder.append (Html.jsqe (href));
				stringBuilder.append ("'\"");

			}

		}

		// extra attributes

		if (extra != null) {

			stringBuilder.append (' ');
			stringBuilder.append (extra);

		}

		// close element and return

		stringBuilder.append (">");

		return stringBuilder.toString ();

	}

	public static
	String magicTr (
			String href,
			boolean selected,
			String target) {

		return magicTr (
			href,
			selected,
			target,
			null,
			null,
			null);
	}

	public static
	String magicTr (
			String href,
			boolean selected) {

		return magicTr (
			href,
			selected,
			null,
			null,
			null,
			null);
	}

	public static
	String magicTd (
			String href,
			String target,
			int colspan,
			String style,
			String extra) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			"<td");

		if (colspan > 1) {

			stringBuilder.append (
				" colspan=\"" + colspan + "\"");

		}

		stringBuilder.append(" style=\"cursor: pointer;" + Html.encode(style) + "\"");
		stringBuilder.append(" onmouseover=\"this.className='hover'\"");
		stringBuilder.append(" onmouseout=\"this.className=null\"");
		if (href != null)
			if (target != null)
				stringBuilder.append(" onclick=\"top.frames['" + Html.jsqe(target)
						+ "'].location='" + Html.jsqe(href) + "'\"");
			else
				stringBuilder.append(" onclick=\"window.location='" + Html.jsqe(href)
						+ "'\"");
		stringBuilder.append(extra);
		stringBuilder.append(">");

		return stringBuilder.toString ();

	}

	public static
	String magicTd (
			String href,
			String target,
			int colspan) {

		return magicTd (
			href,
			target,
			colspan,
			"",
			"");

	}

	public static
	String nbsp (
			String source) {

		return source.replaceAll (
			" ",
			"&nbsp;");

	}

	public static
	String newlineToBr (
			String source) {

		return source.replaceAll (
			"\r\n|\n|\r",
			"<br>");

	}

	public static
	String selectYesNo (
			String name,
			boolean value,
			String trueText,
			String falseText) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			"<select id=\"" + Html.encode (name) + "\" name=\""
			+ Html.encode (name) + "\">\n");

		stringBuilder.append (
			"<option value=\"false\"");

		if (! value) {

			stringBuilder.append (
				" selected");

		}

		stringBuilder.append (
			">" + Html.encode (falseText) + "</option>\n");

		stringBuilder.append (
			"<option value=\"true\"");

		if (value) {

			stringBuilder.append (
				" selected");

		}

		stringBuilder.append (
			">" + Html.encode (trueText) + "</option>\n");

		stringBuilder.append (
			"</select>\n");

		return stringBuilder.toString ();

	}

	public static
	String selectYesNo (
			String name,
			boolean value) {

		return selectYesNo (
			name,
			value,
			"yes",
			"no");

	}

	public static
	String selectYesNo (
			String name,
			String formValue,
			boolean defaultValue) {

		// interpret form value

		boolean value;

		if (
			equal (
				formValue,
				"true")
		) {

			value =
				true;

		} else if (
			equal (
				formValue,
				"false")
		) {

			value =
				false;

		} else if (
			equal (
				formValue,
				null)
		) {

			value =
				defaultValue;

		} else {

			throw new RuntimeException (
				"Invalid form value: " + formValue);

		}

		// render control

		return selectYesNo (
			name,
			value,
			"yes",
			"no");

	}

	public static
	String selectYesNoMaybe (
			String name,
			String value) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			stringFormat (
				"<select name=\"%h\">",
				name));

		stringBuilder.append (
			Html.option (
				"",
				"",
				value));

		stringBuilder.append (
			Html.option (
				"true",
				"yes",
				value));

		stringBuilder.append (
			Html.option (
				"false",
				"no",
				value));

		stringBuilder.append (
			"</select>");

		return stringBuilder.toString ();

	}

	@SneakyThrows (UnsupportedEncodingException.class)
	public static
	String urlEncode (
			String source) {

		return URLEncoder.encode (
			source,
			"utf-8");

	}

	public static <Key>
	String select (
			String name,
			Map<? extends Key,String> options,
			Key selectedValue) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			stringFormat (
				"<select",
				" id=\"%h\"",
				name,
				" name=\"%h\"",
				name,
				">\n"));

		for (Map.Entry<? extends Key,String> optionEntry
				: options.entrySet ()) {

			stringBuilder.append (
				option (
					optionEntry.getKey (),
					optionEntry.getValue (),
					selectedValue));

		}

		stringBuilder.append (
			stringFormat (
				"</select>\n"));

		return stringBuilder.toString ();

	}

	@SuppressWarnings ("null")
	public static <Key>
	String option (
			Key value,
			String label,
			Key defaultValue) {

		boolean selected =
			equal (
				value,
				defaultValue);

		boolean gotValue =
			value != null
			&& ! value.toString ().isEmpty ();

		boolean gotLabel =
			label != null
			&& ! label.isEmpty ();

		return stringFormat (
			"<option",

			"%s",
			gotValue
				? stringFormat (
					" value=\"%h\"",
					value.toString ())
				: "",

			selected
				? " selected"
				: "",

			"%s",
			gotLabel
				? stringFormat (
					">%h</option>\n",
					label)
				: ">\n");

	}

	public static
	String genHtmlColor (
			Object source) {

		int hashCode =
			source != null
				? source.hashCode ()
				: 0;

		return String.format (
			"#%06x",
			hashCode & 0x00ffffff);

	}

}