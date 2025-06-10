package org.teachothers.fishwatchr;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class OverallEvaluation {
	public static final String TAG_EVALUATION = "evaluation";
	public static final String TAG_CATEGORY1 = "category1";
	public static final String TAG_CATEGORY2 = "category2";
	public static final String[] TAG_CATEGORIES = {TAG_CATEGORY1, TAG_CATEGORY2};
	public static final String TAG_COMMENT = "comment";
	public static final String ATTRIBUTE_EVALUATOR = "evaluator";

	private User evaluator = new User("");
	private String comment = "";
	private long timestamp = 0;
	private HashMap<String, EvaluationCategory> evaluationCategories = new HashMap<String, OverallEvaluation.EvaluationCategory>();

	public OverallEvaluation() {
		for(String categoryName : TAG_CATEGORIES) {
			evaluationCategories.put(categoryName, new EvaluationCategory(categoryName));
		}
	}
	
	public OverallEvaluation(User evaluator) {
		this();
		this.evaluator = evaluator;
	}
	
	public OverallEvaluation(Element evaluationElement) {
		this();
		evaluator = new User(evaluationElement.getAttribute(ATTRIBUTE_EVALUATOR));
		setValue(evaluationElement);
	}

	
	public String[] getEvaluationNames(String categoryName) {
		EvaluationCategory category = getEvaluationCategory(categoryName);
		if(category == null) {
			return new String[0];
		} else {
			return category.keySet().toArray(new String[0]);
		}
	}
	
	
	public Evaluation getEvaluation(String categoryName, String evaluationName) {
		return getCategory(categoryName).get(evaluationName);
	}
	
	
	public void setEvaluation(String categoryName, String evaluationName, String score, String comment) {
		EvaluationCategory category = getCategory(categoryName);
		category.setEvaluation(evaluationName, score, comment);
	}
	
	
	public EvaluationCategory getEvaluationCategory(String categoryName) {
		return getCategory(categoryName);
	}

	
	public void setTimestamp() {
		Date now = new Date();
		timestamp = now.getTime();
	}
	
	private void setValue(Element evaluationElement) {

		for(String categoryName : TAG_CATEGORIES) {
			EvaluationCategory category = getCategory(categoryName);
			category.setEvaluation(evaluationElement);
		}

		// comment
		Element commentElement = Util.getFirstChild(evaluationElement, TAG_COMMENT);
		String comment = commentElement.getTextContent();
		comment = comment == null ? "" : comment.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
		setComment(comment);
	}

	
	public EvaluationCategory getCategory(String name) {
		return evaluationCategories.get(name);
	}
	
	public User getEvaluator() {
		return evaluator;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	
	public String toXML() {
		StringBuilder result = new StringBuilder();

		result.append("<" + TAG_EVALUATION + " evaluator=\"" + StringEscapeUtils.escapeXml10(evaluator.getName()) + "\" timestamp=\"" + timestamp + "\">\n");
		result.append(getEvaluationCategory(TAG_CATEGORY1).toXML());
		result.append(getEvaluationCategory(TAG_CATEGORY2).toXML());
		result.append("<" + TAG_COMMENT + ">");
		result.append(StringEscapeUtils.escapeXml10(getComment()));
		result.append("</" + TAG_COMMENT + ">\n");
		result.append("</" + TAG_EVALUATION + ">\n");
		
		return result.toString();
	}
	
	
	
	class EvaluationCategory extends HashMap<String, Evaluation>{
		private static final long serialVersionUID = 1L;

		String name;

		public EvaluationCategory(String name) {
			this.name = name;
		}
		
		public void setEvaluation(Element evaluationElement) {
			Element categoryElement = Util.getFirstChild(evaluationElement, name);
			NodeList categoryItems = categoryElement.getElementsByTagName(Evaluation.TAG);

			for(int i = 0; i < categoryItems.getLength(); i++) {
				Element item = (Element)categoryItems.item(i);
				String evaluationName = item.getAttribute(Evaluation.ATTRIBUTE_NAME);
				String score = item.getAttribute(Evaluation.ATTRIBUTE_VALUE);
				String comment = item.getTextContent();
				
				put(evaluationName, new Evaluation(evaluationName, score, comment));
			}
		}
		
		
		public void setEvaluation(String name, String score, String comment) {
			// overwrite if the key has already existed
			put(name, new Evaluation(name, score, comment));
		}
		
		
		public String toXML() {
			StringBuilder result = new StringBuilder();

			result.append("<" + name + ">\n");
			for (Entry<String, Evaluation> entry : entrySet()) {
				result.append(entry.getValue().toXML()).append("\n");
		    }
			result.append("</" + name + ">\n");
			
			return result.toString();
		}
	}
	
	
	class Evaluation {
		public static final String TAG = "li";
		public static final String ATTRIBUTE_NAME = "name";
		public static final String ATTRIBUTE_VALUE = "value";

		String name;
		String score;
		String comment;

		public Evaluation(String name, String score, String comment) {
			this.name = name;
			this.score = score;
			this.comment = comment;
		}

		
		public String getName() {
			return name;
		}
		
		public String getScore() {
			return score;
		}

		public void setScore(String score) {
			this.score = score;
		}
		
		public String getComment() {
			return comment;
		}
		
		public void setComment(String comment) {
			this.comment = comment;
		}
		
		public String toXML() {
			StringBuilder result = new StringBuilder();
			result.append("<" + TAG + " ");
			result.append(ATTRIBUTE_NAME + "=\"" + StringEscapeUtils.escapeXml10(name) + "\" ");
			result.append(ATTRIBUTE_VALUE + "=\"" + StringEscapeUtils.escapeXml10(score) + "\">");
			result.append(StringEscapeUtils.escapeXml10(comment));
			result.append("</" + TAG + ">");
			
			return result.toString();
		}
	}
}
