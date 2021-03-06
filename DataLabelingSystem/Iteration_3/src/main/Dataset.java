package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

public class Dataset {
    // attributes of the Dataset class
    private int id;
    private String name;
    private int maxLabel;
    private List<Instance> instances;
    private List<Label> labels;
    private List<User> assignedUsers;

    // constructor of the Dataset class
    public Dataset(int id, String name, int maxLabel) {
        this.id = id;
        this.name = name;
        this.maxLabel = maxLabel;
        this.instances = new ArrayList<Instance>();
        this.labels = new ArrayList<Label>();
        this.assignedUsers = new ArrayList<User>();
    }

    // creates instance objects and add them to the corresponding data structures
    public void addInstances(JSONArray instances) {
        for (int i = 0; i < instances.size(); i++) {
            Map<String, Object> currentInstance = (HashMap<String, Object>) instances.get(i);
            long instanceId = (long) currentInstance.get("id");
            String instanceText = (String) currentInstance.get("instance");
            Instance instanceObject = new Instance((int) instanceId, instanceText);
            instanceObject.setDataset(this);
            this.instances.add(instanceObject);
        }
    }

    // creates label objects and add them to the corresponding data structures
    public void addLabels(JSONArray labels) {
        for (int i = 0; i < labels.size(); i++) {
            Map<String, Object> currentLabel = (HashMap<String, Object>) labels.get(i);
            long labelId = (long) currentLabel.get("label id");
            String labelText = (String) currentLabel.get("label text");
            Label labelObject = new Label((int) labelId, labelText);
            labelObject.setDataset(this);
            this.labels.add(labelObject);
        }
    }

    // add the given user to the assigned users list
    public void addAssignedUser(User user) {
        this.assignedUsers.add(user);
    }

    // returns the id of this dataset
    public int getId() {
        return this.id;
    }

    // returns the name of this dataset
    public String getName() {
        return this.name;
    }

    // returns the max label of this dataset
    public int getMaxLabel() {
        return this.maxLabel;
    }

    // returns the instance list of this dataset
    public List<Instance> getInstances() {
        return this.instances;
    }

    // returns an Instance object given the instance id
    public Instance getInstance(int instanceId) {
        Instance instance = null;
        for (Instance currentInstance : this.instances) {
            if (currentInstance.getId() == instanceId) {
                instance = currentInstance;
            }
        }
        return instance;
    }

    // returns the label list list of this dataset
    public List<Label> getLabels() {
        return this.labels;
    }

    // returns the assigned user list of this dataset
    public List<User> getAssignedUsers() {
        return this.assignedUsers;
    }

    // returns the completeness percentage of this dataset
    public double getCompleteness() {
        int instanceLabeledCount = 0;
        for (Instance instance : this.instances) {
            if (instance.getTotalLabelAssignments() >= 1) {
                instanceLabeledCount++;
            }
        }
        return instanceLabeledCount * 100.0 / instances.size();
    }

    // returns the final distribution of labels in this dataset
    public Map<String, Double> getDistribution(List<LabelAssignment> labelAssignments) {
        // first initialize counts to 0
        Map<String, Double> labelFreqs = new HashMap<String, Double>();
        for (Label label : this.labels) {
            labelFreqs.put(label.getText(), 0.0);
        }

        // count labels in label assignments
        for (LabelAssignment labelAssignment : labelAssignments) {
            for (Label label : labelAssignment.getAssignedLabels()) {
                double currentCount = labelFreqs.get(label.getText());
                labelFreqs.put(label.getText(), currentCount + 1);
            }
        }

        // normalize
        for (Map.Entry<String, Double> entry : labelFreqs.entrySet()) {
            labelFreqs.put(entry.getKey(), (entry.getValue() * 100.0) / labelAssignments.size());
        }
        return labelFreqs;
    }

    // returns the number of unique instances labeled for each class label(for this dataset)
    public Map<String, Integer> getUniqueInstances(List<LabelAssignment> labelAssignments) {
        // first intialize counts to empty list
        Map<String, ArrayList<String>> uniqueInstances = new HashMap<String, ArrayList<String>>();
        for (Label label : this.labels) {
            uniqueInstances.put(label.getText(), new ArrayList<String>());
        }

        // add the unique instances to a list
        for (LabelAssignment labelAssignment : labelAssignments) {
            for (Label label : labelAssignment.getAssignedLabels()) {
                String instanceText = labelAssignment.getInstance().getText();
                if (!uniqueInstances.get(label.getText()).contains(instanceText)) {
                    uniqueInstances.get(label.getText()).add(instanceText);
                }
    
            }
        }

        // change the list to size of list
        Map<String, Integer> uniqueInstancesFreq = new HashMap<String, Integer>();
        for (Map.Entry<String, ArrayList<String>> entry : uniqueInstances.entrySet()) {
            uniqueInstancesFreq.put(entry.getKey(), entry.getValue().size());
        }
        return uniqueInstancesFreq;
    }

    // returns the total number of assigned users to this dataset
    public int getTotalAssignedUsers() {
        return this.assignedUsers.size();
    }

    // returns the completeness percentage of each user from this dataset
    public Map<String, Double> getUserCompletenessPercentange() {
        // first find out the total number of labelings done by assigned users in this dataset
        Map<String, Double> userCompleteness = new HashMap<String, Double>();
        for (User user : this.assignedUsers) {
			Map<String, Double> datasetCompleteness = user.getCompletenessPercentage();
			userCompleteness.put(user.getName(), datasetCompleteness.get("dataset" + this.getId()));
        }
        return userCompleteness;
    }

    // returns the consistency percentage of each user from this dataset
    public Map<String, Double> getUserConsistencyPercentage() {
		Map<String, Double> userConsistencyPercentages = new HashMap<String, Double>();

		// for each user which was assigned to this dataset
		for (User user : this.assignedUsers) {
			Map<Instance, List<List<Label>>> instanceFrequency = new HashMap<Instance, List<List<Label>>>();
			// for each instance which was labeled by this user
			for (LabelAssignment labelAssignment : user.getLabelAssignments()) {
				// get rid of instances which are not from this dataset
				if (labelAssignment.getInstance().getDataset() != this) {
					continue;
				} else if (!instanceFrequency.containsKey(labelAssignment.getInstance())) {
					instanceFrequency.put(labelAssignment.getInstance(), new ArrayList<>());
				}
				instanceFrequency.get(labelAssignment.getInstance()).add(labelAssignment.getAssignedLabels());
			}

			int totalRecurrentLabelings = 0;
			int totalConsistentRecurrentLabelings = 0;
	
			for (Map.Entry<Instance, List<List<Label>>> entry : instanceFrequency.entrySet()) {
				if (entry.getValue().size() > 1) {
					Set<List<Label>> uniqueLabels = new HashSet<List<Label>>(entry.getValue());
					if (uniqueLabels.size() == 1) {
						totalConsistentRecurrentLabelings++;
					}
					totalRecurrentLabelings++;
				}			
			}

			double consistencyPercentage;
			if (totalRecurrentLabelings == 0) {
				consistencyPercentage = 0.0;
			} else {
				consistencyPercentage = totalConsistentRecurrentLabelings * 100.0 / totalRecurrentLabelings;
			}
	
			userConsistencyPercentages.put("user" + user.getId(), consistencyPercentage);
		}

		return userConsistencyPercentages;
    }
}
