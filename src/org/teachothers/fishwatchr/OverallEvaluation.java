package org.teachothers.fishwatchr;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OverallEvaluation {
	private String evaluatorName = "";
	private String comment = "";
	private HashMap<String, String> target1 = new HashMap<String, String>();
	private HashMap<String, String> target2 = new HashMap<String, String>();

	public OverallEvaluation(String evaluatorName) {
		this.evaluatorName = evaluatorName;
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

	public void setTarget1(String key, String value) {
		target1.put(key, value);
	}

	
	public void setTarget2(String key, String value) {
		target2.put(key, value);
	}
	
	public String getTarget1(String key) {
		return target1.get(key);
	}

	public String getTarget2(String key) {
		return target2.get(key);
	}
	
	public static OverallEvaluation buildEvaluation(Element evaluationElement) {
		
		String evaluatorName = evaluationElement.getAttribute("evaluator");

		Element target1Element = Util.getFirstChild(evaluationElement, "target1");
		Element target2Element = Util.getFirstChild(evaluationElement, "target2");
		Element commentElement = Util.getFirstChild(evaluationElement, "comment");

		if(target1Element == null && target2Element == null) {
			return null;
		}
		
		OverallEvaluation evaluation = new OverallEvaluation(evaluatorName);
		
		NodeList target1Items = target1Element.getElementsByTagName("li");
		NodeList target2Items = target2Element.getElementsByTagName("li");
		
		// target1
		for(int i = 0; target1Items != null && i < target1Items.getLength(); i++) {
			Element item = (Element)target1Items.item(i);
			evaluation.setTarget1(item.getAttribute("name"), item.getAttribute("value"));
		}
		
		// target2
		for(int i = 0; target2Items != null && i < target2Items.getLength(); i++) {
			Element item = (Element)target2Items.item(i);
			evaluation.setTarget2(item.getAttribute("name"), item.getAttribute("value"));
		}

		// comment
		String comment = commentElement.getTextContent();
		comment = comment == null ? "" : comment.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
		evaluation.setComment(comment);
		
		
		

		return evaluation;
	}
}
