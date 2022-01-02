package org.teachothers.fishwatchr;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class OverallEvaluation {
	public static final String TAG_EVALUATION = "evaluation";
	public static final String TAG_CATEGORY1 = "target1";
	public static final String TAG_CATEGORY2 = "target2";
	public static final String[] TAG_CATEGORIES = {TAG_CATEGORY1, TAG_CATEGORY2};
	public static final String TAG_COMMENT = "comment";
	public static final String TAG_LI = "li";
	public static final String ATTRIBUTE_EVALUATOR = "evaluator";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_VALUE = "value";
	public static final String ATTRIBUTE_COMMENT = "comment";
	
	private String evaluatorName = "";
	private String comment = "";
	private HashMap<String, EvaluationCategory> evaluationCategories = new HashMap<String, OverallEvaluation.EvaluationCategory>();

	public OverallEvaluation() {
		for(String categoryName : TAG_CATEGORIES) {
			evaluationCategories.put(categoryName, new EvaluationCategory(categoryName));
		}
	}
	
	public OverallEvaluation(String evaluatorName) {
		this();
		this.evaluatorName = evaluatorName;
	}
	
	public OverallEvaluation(Element evaluationElement) {
		this();
		evaluatorName = evaluationElement.getAttribute(ATTRIBUTE_EVALUATOR);
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
	
	
	public EvaluationCategory getEvaluationCategory(String categoryName) {
		return getCategory(categoryName);
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
	
	public String getEvaluatorName() {
		return evaluatorName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	
	class EvaluationCategory extends HashMap<String, Evaluation>{
		private static final long serialVersionUID = 1L;
		String name;

		public EvaluationCategory(String name) {
			this.name = name;
		}
		
		public void setEvaluation(Element evaluationElement) {
			System.err.println("k:" + name);
			Element categoryElement = Util.getFirstChild(evaluationElement, name);
			NodeList categoryItems = categoryElement.getElementsByTagName(TAG_LI);

			for(int i = 0; i < categoryItems.getLength(); i++) {
				Element item = (Element)categoryItems.item(i);
				String evaluationName = item.getAttribute(ATTRIBUTE_NAME);
				String score = item.getAttribute(ATTRIBUTE_VALUE);
				String comment = item.getAttribute(ATTRIBUTE_COMMENT);
				
				put(evaluationName, new Evaluation(evaluationName, score, comment));
			}
		}
	}
	
	
	class Evaluation {
		String name;
		String  score;
		String comment;

		public Evaluation(String name, String score, String comment) {
			this.name = name;
			this.score = score;
			this.comment = comment;
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
		
		
	}
}
