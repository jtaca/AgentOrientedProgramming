//package exercise_v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
//import java.math.Float;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;




public class Agent {

	/**********************************/
	/******* A: AGENT BEHAVIOR ********/
	/**********************************/
	
	public HashMap<String, Object> options = new HashMap<String, Object>();
	public Integer currentStep, restart , flexibility;
	public Float memoryFactor, totalGain, denominator, restartHistory ;
	public TreeMap<String, LinkedList<Task>> state;
	public TreeMap<String, LinkedList<Task>> choices  =new TreeMap<String, LinkedList<Task>>();
	
	public LinkedList<Object> flexibleChoices  =new LinkedList<Object>();
	
	public LinkedList<String> KEYWORDS = new LinkedList<String>(Arrays.asList(
					"cycle","decision","restart", "memory-factor",
					"agents","concurrency-penalty", "concurrentCost"));
	//public LinkedList<ServoAgent> society;
	//public Runtime rt = Runtime.getRuntime();
	@SuppressWarnings("deprecation")
	public Agent(String[] options) {
		LinkedList<String> auxOptions = new LinkedList<String>(Arrays.asList(options));
		
		currentStep = 0; totalGain = new Float(0);
		//assigning values in the hash table for options
		for(String option: auxOptions) {
			for(String word : KEYWORDS) {
				if(option.contains(word)) {
					if(word.equalsIgnoreCase("decision")) {
						// get type of decision
						this.options.put(word,option.split("\\=")[1]);
					}else if(word.equalsIgnoreCase("agents")){
						//uncheck this for tests
						//System.out.println(option.split("\\[")[1].split("\\]")[0].split(","));
						LinkedList<String> agentNames = new LinkedList<String>(
								Arrays.asList(option.split("\\[")[1].split("\\]")[0].split(",")));
						//System.out.println(agentNames.toString());
						this.options.put(word,agentNames);
					}else{
							@SuppressWarnings("deprecation")
							Float value = new Float(option.split("\\=")[1]);
							this.options.put(word,value);
			        }
				}
			}	
		}
		//Initialize states for all agents
		state = new TreeMap<String, LinkedList<Task>>();
		if(((String)this.options.get("decision")).contains("society")) {
			@SuppressWarnings("unchecked")
			LinkedList<String> linkedList = (LinkedList<String>) this.options.get("agents");
			for(String agent: linkedList) {
				state.put(agent, new LinkedList<Agent.Task>());
				choices.put(agent, new LinkedList<Agent.Task>());
				//create and add each agent ---- new ServoAgent(); society.add(e);
			}
			//System.out.println(state);
		}else{
			state.put("A", new LinkedList<Agent.Task>());
			choices.put("A", new LinkedList<Agent.Task>());
		}
			
		//print options
		//System.out.println(this.options);	

		this.memoryFactor = (Float) this.options.get("memory-factor");
		try {
			this.restart = (Integer) ((Float) (this.options.get("restart"))).intValue() ;
			//System.out.println(restart);
		} catch (Exception e) {
			restart = null;
		}
		
	}
	
	public class TaskPair{
		public Task task1, task2;
		public float weight1, weight2;
		
		public TaskPair(Task task1, Task task2, float weight1, float weight2) {
			super();
			this.task1 = task1;
			this.task2 = task2;
			this.weight1 = weight1;
			this.weight2 = weight2;
		}
		@Override
		public String toString() {
			return "TaskPair [task1=" + task1 + ", task2=" + task2 + ", weight1=" + weight1 + ", weight2=" + weight2
					+ "]";
		}
		public Task getTask1() {
			return task1;
		}
		public void setTask1(Task task1) {
			this.task1 = task1;
		}
		public Task getTask2() {
			return task2;
		}
		public void setTask2(Task task2) {
			this.task2 = task2;
		}
		public float getWeight1() {
			return weight1;
		}
		public void setWeight1(float weight1) {
			this.weight1 = weight1;
		}
		public float getWeight2() {
			return weight2;
		}
		public void setWeight2(float weight2) {
			this.weight2 = weight2;
		}
		
		
	}
	
	@SuppressWarnings("deprecation")
	public void perceive(String input) {
		//observe result of action taken
		if(input.contains("A")) {
			String agent = input.split(" ")[0];
			
			if(((String)this.options.get("decision")).contains("flexible")) {
				
				//trigger o melhor AKA o anterior e 0 ou negativo-> toggle flexibility = 1;
				
				if(input.split(" ")[1].split("\\=")[1].contains("{")){
					//read something like this u={T4=-1,T5=5}
					String aux = input.split(" ")[1].split("\\{")[1].replaceFirst(".$","");
					Float observation1 = new Float(aux.split(",")[0].split("\\=")[1]);
					Float observation2 = new Float(aux.split(",")[1].split("\\=")[1]);
					
					//System.out.println(observation1);
					//System.out.println(observation2);
					
					TaskPair pair = (TaskPair)this.flexibleChoices.getLast();
					
					//divide the gain in the observation by the weight
					this.totalGain = this.totalGain+observation1*pair.weight1;
					this.totalGain = this.totalGain+observation2*pair.weight2;
					
					//add history to each task
					for(Task t:state.get(agent)) {
						if(t.action.compareToIgnoreCase(pair.task1.action) == 0) {
							t.addObservation(agent, observation1,new Float(currentStep));
							t.calculateCurrentUtility();
						}
						if(t.action.compareToIgnoreCase(pair.task2.action) == 0) {
							t.addObservation(agent, observation2,new Float(currentStep));
							t.calculateCurrentUtility();
						}
					}
					
					//System.out.println("TotalGain: "+totalGain.toString());
					
					//Float observation = new Float(input.split(" ")[1].split("\\=")[1]);
					
					
				}else {
					String action = ((Task)this.flexibleChoices.getLast()).action;
					Float observation = new Float(input.split(" ")[1].split("\\=")[1]);
					this.totalGain = this.totalGain+observation;
					for(Task t:state.get(agent)) {
						if(t.action.compareToIgnoreCase(action) == 0) {
							t.addObservation(agent, observation,new Float(currentStep));
							t.calculateCurrentUtility();
						}
					}
				}
				
				
				
			} else if(((String)this.options.get("decision")).contains("society")) {
				if(((String)this.options.get("decision")).contains("homogeneous")) {
					
					String action = ( this.choices.get(agent)).getLast().action;
					@SuppressWarnings("deprecation")
					Float observation = new Float(input.split(" ")[1].split("\\=")[1]);
					
					//System.out.println(observation);
					this.totalGain += observation;
					
					Set<String> agents = state.keySet();
					for(String key: agents) {
						for(Task t:state.get(key)) {
							if(t.action.compareToIgnoreCase(action) == 0) {
								t.addObservation(agent, observation, new Float(currentStep));
								t.calculateCurrentUtility();
							}
						}
					}	
				
				}
				if(((String)this.options.get("decision")).contains("heterogeneous")) {
					
					String action = ( this.choices.get(agent)).getLast().action;
					Float observation = new Float(input.split(" ")[1].split("\\=")[1]);
					//System.out.println(observation);
					this.totalGain  += observation;
					
					for(Task t:state.get(agent)) {
						if(t.action.compareToIgnoreCase(action) == 0) {
							t.addObservation(agent, observation, new Float(currentStep));
							t.calculateCurrentUtility();
						}
					}
				}
			}else {
				String action = ( this.choices.get("A")).getLast().action;
				Float observation = new Float(input.split(" ")[1].split("\\=")[1]);
				this.totalGain = this.totalGain+observation;
				for(Task t:state.get(agent)) {
					if(t.action.compareToIgnoreCase(action) == 0) {
						t.addObservation(agent, observation,new Float(currentStep));
						t.calculateCurrentUtility();
					}
				}
			}
			//System.out.println("TotalGain: "+totalGain.toString());
			

			//Receive new action
		}else{
			String action = input.split(" ")[0];
			Float prediction = new Float(input.split(" ")[1].split("\\=")[1]);
			
			if(((String)this.options.get("decision")).contains("society")) {
				
				// think about how societies decide if() {}
				//System.out.println("add task to each agent set of states");
				
				Set<String> agents = state.keySet();
				for(String key: agents) {
					state.get(key).add(new Task(action,prediction));
				}
				
			}else {
				state.get("A").add(new Task(action,prediction));
			}
		}
		//System.out.println(state);
	}
	
	public static <T> Set<List<T>> getCombinations(List<List<T>> lists) {
	    Set<List<T>> combinations = new HashSet<List<T>>();
	    Set<List<T>> newCombinations;

	    int index = 0;

	    // extract each of the integers in the first list
	    // and add each to ints as a new list
	    for(T i: lists.get(0)) {
	        List<T> newList = new ArrayList<T>();
	        newList.add(i);
	        combinations.add(newList);
	    }
	    index++;
	    while(index < lists.size()) {
	        List<T> nextList = lists.get(index);
	        newCombinations = new HashSet<List<T>>();
	        for(List<T> first: combinations) {
	            for(T second: nextList) {
	                List<T> newList = new ArrayList<T>();
	                newList.addAll(first);
	                newList.add(second);
	                newCombinations.add(newList);
	            }
	        }
	        combinations = newCombinations;

	        index++;
	    }

	    return combinations;
	}
	
	@SuppressWarnings("unchecked")
	public void decideAndAct() {
		if(((String)this.options.get("decision")).contains("flexible")) {
			
			
			
			LinkedList<Task> auxTaskList = state.get("A");
			Task bestTask = auxTaskList.getFirst();
			bestTask.calculateRealUtility();
			for(Task t: auxTaskList) {
				t.calculateRealUtility();
				
				if (t.realUtility.compareTo(bestTask.realUtility) > 0 
						|| (t.beenNegative && t.lastNegative.compareTo(bestTask.lastNegative) > 0)) {
					bestTask = t;	
				}
				//System.out.println(t.action+" = "+ t.realUtility);
			}
			TaskPair bestTaskPair = null; Task otherBestTask = null;
			float weight1 = 0, weight2 = 0;
			if(bestTask.beenNegative) {
				
				LinkedList<Task> neverNegative = new LinkedList<Agent.Task>();
				for(Task t: auxTaskList) {
					if (!t.beenNegative) {
						neverNegative.add(t);	
					}
				}
				if(neverNegative.isEmpty()) {
					for(Task t: auxTaskList) {
							neverNegative.add(t);
					}
					neverNegative.remove(bestTask);
				}
				
				otherBestTask= neverNegative.getFirst();
				for(Task t: neverNegative) {
					t.calculateRealUtility();
					
					if (t.realUtility.compareTo(otherBestTask.realUtility) > 0) {
						otherBestTask = t;	
					}
					//System.out.println("otherBestTask"+t.action+" = "+ t.realUtility);
				}
				if(otherBestTask != null) {
					//calculate weight of each one
					for(Float i = (float) 0; i.compareTo((float) 1)<0; i+=0) {
						Float j = 1-i;
						bestTask.calculateCurrentUtility(); otherBestTask.calculateCurrentUtility();
						float result = i*bestTask.lastNegative+j*otherBestTask.currentUtility;
						
						float epsilon = (float) 0.0001;
//						if(Math.abs(i-0.63)<epsilon) {
//							System.out.println("i(1): "+ i+", j(2): "+j+", result: "+ result);
//							
//						}
						
						if(Math.abs(i*bestTask.lastNegative + j*otherBestTask.currentUtility) < epsilon) {//result == (float) 0){
							weight1 = i; weight2 = j;
						}
						i+=(float)0.00001;
					}
					
					bestTaskPair = new TaskPair(bestTask,otherBestTask,weight1,weight2);
					DecimalFormat df = new DecimalFormat("0.00");
					System.out.println("{"+bestTask.action+"="+df.format(weight1)+","+otherBestTask.action+"="+df.format(weight2)+"}");
				}
				
			}
//			else {
//				System.out.println();
//			}
			
			
			
			
			//Object pair = ;
			if(!this.flexibleChoices.isEmpty()) {
				if (this.flexibleChoices.getLast() instanceof TaskPair) {
					
					if(((TaskPair)this.flexibleChoices.getLast()).task1.action.compareTo(bestTask.action) != 0
						||(otherBestTask != null && ((TaskPair)this.flexibleChoices.getLast()).task1.action.compareTo(otherBestTask.action) !=0)) {
						((TaskPair)this.flexibleChoices.getLast()).task1.cyclesWaited = 0;
					}
					if(((TaskPair)this.flexibleChoices.getLast()).task2.action.compareTo(bestTask.action) != 0
						||(otherBestTask != null && ((TaskPair)this.flexibleChoices.getLast()).task2.action.compareTo(otherBestTask.action) !=0)) {
						((TaskPair)this.flexibleChoices.getLast()).task2.cyclesWaited = 0;
					}
				}else {
					if(((Task)this.flexibleChoices.getLast()).action.compareTo(bestTask.action) != 0
						||(otherBestTask != null && ((Task)this.flexibleChoices.getLast()).action.compareTo(otherBestTask.action) != 0)) {
						((Task)this.flexibleChoices.getLast()).cyclesWaited = 0;
					}
				}
			}
			
			//System.out.println("bestTaskPair: "+bestTaskPair);
			
			//System.out.println("bestTask"+bestTask);//+", negative before? "+ bestTask.beenNegative );
			
			if(bestTask.cyclesWaited < restart) {
				bestTask.cyclesWaited+=1;
			}
			if(bestTaskPair != null && bestTaskPair.task2.cyclesWaited < restart) {
				bestTaskPair.task2.cyclesWaited+=1;
			}
			if(bestTaskPair != null) {
				flexibleChoices.add(bestTaskPair);
			}else {
				flexibleChoices.add(bestTask);
			}
				
			
			
			//System.out.println(choices);
			currentStep++;
			
		}else if(((String)this.options.get("decision")).contains("society")) {
			Float concurrentCost;
			try {
				concurrentCost = (Float) (options.get("concurrency-penalty"));
				if(concurrentCost == null) {
					concurrentCost = (Float) (options.get("concurrentCost"));
				}
				if(concurrentCost == null) {
					concurrentCost = (float) 0;
				}
			}catch (Exception e) {
				concurrentCost = (float) 0;
			}
			
			
			if(concurrentCost.compareTo((float) 0) > 0) {

				List<List<String>> lists = new LinkedList<List<String>>();
				Set<String> auXagents = state.keySet();
				List<String> agents = new LinkedList<>(),tasks = new LinkedList<>() ; 
		        for (String t : auXagents) 
		        	agents.add(t); 
				LinkedList<Task> auXtasks = state.get(state.firstKey());
				for (Task t : auXtasks) 
		        	tasks.add(t.action); 
				//lists.add(agents);
				for(int i = 0; i<auXagents.size(); i++) {
					lists.add(tasks);
				}
				///////////////
				
				Set<List<String>> combs = getCombinations(lists);
				
				///////////
				
				//lists = null; //try erase some memory
				//printObjectSize("combs Size: "+combs);
				
				//Float bestUtility = new Float(0); 
				
				int bestListindex = 0, currentListIndex = 0;
				Float bestListUtility = null;
				
				class ListComparator<T extends Comparable<T>> implements Comparator<List<T>> {

					  @Override
				  public int compare(List<T> o1, List<T> o2) {
				    for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
				      int c = o1.get(i).compareTo(o2.get(i));
				      if (c != 0) {
				        return c;
				      }
				    }
				    return Integer.compare(o1.size(), o2.size());
				  }

				}
				
				////////////////////////////
				@SuppressWarnings("rawtypes")
				ArrayList<ArrayList<String>> stringsLists = new ArrayList(combs);
				
				
				//combs = null; // erase data
				Collections.sort(stringsLists, new ListComparator<>());
				
				
			    for(List<String> list : stringsLists) {
			    	@SuppressWarnings("deprecation")
					Float currentListUtility = new Float(0); 
			        //System.out.println(list.toString());
			        int indiceAgent = 0;
			       // List<String> auXchoices = new LinkedList<>();
			        for(String agent: agents) {
			        	
			        	@SuppressWarnings("deprecation")
						Float occurrencesT = new Float(Collections.frequency(list,list.get(indiceAgent) ));
			        	
			        	LinkedList<Task> auxTaskList = state.get(agent);
			        	Task auxTask = null;
			        	for(Task t :auxTaskList) {
			        		if(t.action.compareTo(list.get(indiceAgent))==0) {
			        			auxTask = t;
			        		}
			        	}
			        	//auxTaskList = null;
			        	////////////////////////////////////////////////////////////////////////////////////////////
			        	//System.out.println(auxTask.action+"="+auxTask.calculateRealUtilityWithPenalty(concurrentCost * (occurrencesT-1)));
			        	//calculate utility of each element
			        	currentListUtility = currentListUtility+auxTask.calculateRealUtilityWithPenalty(
			        			concurrentCost*(occurrencesT-1));
			        	//auXchoices.add(list.get(indiceAgent));
			        	indiceAgent++;
			        }
			        //////////////////////////////////////////////////////////////////
			        //System.out.println(list.toString()+", Utility: "+ currentListUtility);
			        if( bestListUtility != null && currentListUtility.compareTo(bestListUtility)>0) {
			        	bestListUtility = currentListUtility;
			        	bestListindex = currentListIndex;
			        }else if( bestListUtility == null){
			        	bestListUtility = currentListUtility;
			        	bestListindex = currentListIndex;
			        }
			        currentListIndex+=1;
			    }
			    
				
			    //////////////////////////////////////////////////////////
			    //System.out.println("Best: "+stringsLists.get(bestListindex).toString()+", Utility: "+ bestListUtility);
			    int indiceAgent = 0;
			    //printObjectSize("stringsLists Size: "+stringsLists);
			    for(String agent: agents) {
			    	List<String> list = (List<String>) stringsLists.get(bestListindex);
			    	Task bestTask = null;
			    	LinkedList<Task> auxTaskList = state.get(agent);
			    	for(Task t :auxTaskList) {
		        		if(t.action.compareTo(list.get(indiceAgent))==0) {
		        			bestTask = t;
		        		}
		        	}
			    	
			    	if(!choices.get(agent).isEmpty() && ( choices.get(agent)).getLast().action.compareTo(bestTask.action) != 0) {
						( choices.get(agent)).getLast().cyclesWaited = 0;
					}
			    	Integer restart;
					try {
						restart = (Integer) ((Float) (options.get("restart"))).intValue() ;
						if(bestTask.cyclesWaited < restart) {
							bestTask.cyclesWaited+=1;
						}
					} catch (Exception e) {
						restart = null;
					}
					choices.get(agent).add(bestTask);
			    	indiceAgent++;
			    }
			   
			    currentStep++;
			}else {
				Set<String> agents = state.keySet();
				for(String key: agents) {
					LinkedList<Task> auxTaskList = state.get(key);
					Task bestTask = auxTaskList.getFirst();
					bestTask.calculateRealUtility();
					//System.out.println("Agent: "+ key);
					for(Task t: auxTaskList) {
						t.calculateRealUtility();					
						if (t.realUtility.compareTo(bestTask.realUtility) > 0) {
							bestTask = t;
						}else if(t.realUtility.compareTo(bestTask.realUtility) == 0 && t.compareTo(bestTask) < 0) {
							bestTask = t;
						}
					}
					if(!choices.get(key).isEmpty() && ( choices.get(key)).getLast().action.compareTo(bestTask.action) != 0) {
						( choices.get(key)).getLast().cyclesWaited = 0;
					}
					
					Integer restart;
					try {
						restart = (Integer) ((Float) (options.get("restart"))).intValue() ;
						if(bestTask.cyclesWaited < restart) {
							bestTask.cyclesWaited+=1;
						}
					} catch (Exception e) {
						restart = null;
					}
					( choices.get(key)).add(bestTask);
					
				}
				currentStep++;
			}	
		}else {
		
			LinkedList<Task> auxTaskList = state.get("A");
			Task bestTask = auxTaskList.getFirst();
			bestTask.calculateRealUtility();
			for(Task t: auxTaskList) {
					t.calculateRealUtility();
					
					if (t.realUtility.compareTo(bestTask.realUtility) > 0) {
						bestTask = t;
					}
					//System.out.println(t.action+" = "+ t.realUtility);
			}
			if(!choices.get("A").isEmpty() 
					&& (choices.get("A")).getLast().action.compareTo(bestTask.action) != 0) {
				(choices.get("A")).getLast().cyclesWaited = 0;
			}
			
			Integer restart;
			try {
				restart = (Integer) ((Float) (options.get("restart"))).intValue() ;
				if(bestTask.cyclesWaited < restart) {
					bestTask.cyclesWaited+=1;
				}
			} catch (Exception e) {
				restart = null;
			}
			( choices.get("A")).add(bestTask);
			
			//System.out.println(choices);
			currentStep++;
		}	
	}
	
	public String printArray(LinkedList<?> list) {
		String output = "{" ;
		
		for(Object obj: list) {
			output += obj.toString();
			output += ",";
		}
		output = output.replaceFirst(".$","");
		output+="}";
		return output;
	}
	
	
	public String recharge() {
		String output = new String();
		
		output += "state=";
		if(((String)this.options.get("decision")).contains("society")) {
			output+="{";
			for(String key: state.keySet()) {
				output += key+"=";
				output += printArray(state.get(key));
				
				output += ",";		
			}
			output = output.replaceFirst(".$","");
			output+="}";
		}else{
			output += printArray(state.get("A"));
		}
		output+=" gain=";
		DecimalFormat df = new DecimalFormat("0.00");
		output+= df.format(totalGain);
		return output;
	}
	
	
	public class TaskHistory{
		public Float value;
		public Float step; 
		
		public TaskHistory( Float value, Float step) {
			super();
			this.value = value;
			this.step = step;
			
		}

		@Override
		public String toString() {
			return "[ " + value + ", "+ step +"]";
		}
		
	}
	
	public class Task implements Comparable<Task>{
		
		public Float lastNegative;
		public LinkedList<TaskHistory> history = new LinkedList<Agent.TaskHistory>();
		public String action;
		public Float initialUtility, currentUtility, realUtility;
		public Integer cyclesWaited;
		public Boolean beenNegative;
		
		public Task(String action, Float initialUtility) {
			super();
			this.lastNegative = (float) 0;
			this.action = action;
			this.initialUtility = initialUtility;
			this.cyclesWaited = 0;
			this.currentUtility = initialUtility;
			this.beenNegative = false;
			
			
			calculateRealUtility();
		} 
		
		public void calculateRealUtility() {
			Integer remainingCycles = (Integer)((Float) (options.get("cycle"))).intValue() - currentStep;
			calculateCurrentUtility();
			if(restart != null && restart > 0) {
				
				@SuppressWarnings("deprecation")
				Float aux = new Float(remainingCycles - restart + cyclesWaited);
				//System.out.println(aux);
				
				realUtility = currentUtility*aux;
				
				
				//remove round if needed
				//realUtility = (float) (Math.round(realUtility * 100.0) / 100.0);

			}else {
				realUtility = currentUtility;
				
			}
			
			
			//Utitility * (nr ciclos q faltam - restart +tempo esperado na task atual) = > Real_Utility
			
		}
		
		public Float calculateRealUtilityWithPenalty(Float d) {
			Integer remainingCycles = (Integer)((Float) (options.get("cycle"))).intValue() - currentStep;
			calculateCurrentUtility();
			
			@SuppressWarnings("unused")
			Float realUtilityPenal, currentUtilityPenal;
			if(restart != null && restart > 0) {
				
				@SuppressWarnings("deprecation")
				Float aux = new Float(remainingCycles - restart + cyclesWaited);
				//System.out.println(aux);
				//System.out.println(action+" , "+aux);
				realUtilityPenal = (currentUtility-d)*aux;
				
				
				//remove round if needed
				//realUtility = (float) (Math.round(realUtility * 100.0) / 100.0);

			}else {
				realUtilityPenal = currentUtility-d;
				
			}
			return realUtilityPenal;
			
			//Utitility * (nr ciclos q faltam - restart +tempo esperado na task atual) = > Real_Utility
			
		}
		
		public void addObservation(String agentName, Float value, Float step) {
			history.add(new TaskHistory( value, step));
			if(value <= 0) {
				this.beenNegative = true;
				this.lastNegative = value;
			}
			
		}
		
		public BigDecimal powerBig(BigDecimal base, BigDecimal exponent) {

		    BigDecimal ans=  new BigDecimal(1.0);
		    BigDecimal k=  new BigDecimal(1.0);
		    BigDecimal t=  new BigDecimal(-1.0);
		    BigDecimal no=  new BigDecimal(0.0);

		    if (exponent != no) {
		        BigDecimal absExponent =  exponent.signum() > 0 ? exponent : t.multiply(exponent);
		        while (absExponent.signum() > 0){
		            ans =ans.multiply(base);
		            absExponent = absExponent.subtract(BigDecimal.ONE);
		        }

		        if (exponent.signum() < 0) {
		            // For negative exponent, must invert
		            ans = k.divide(ans);
		        }
		    } else {
		        // exponent is 0
		        ans = k;
		    }

		    return ans;
		}
		
		@SuppressWarnings("deprecation")
		public void calculateCurrentUtility() {
			MathContext mc = new MathContext(100);
			if(history.isEmpty()) {
				currentUtility = initialUtility;
			}else {
				// calculating the denominator  NEEDS Testing
				if(memoryFactor != null) {
					@SuppressWarnings("unused")
					BigDecimal denominator = new BigDecimal(0); 
					BigDecimal numerator = new BigDecimal(0);
					
					for(TaskHistory t: history) {
						//isto funciona
						denominator = denominator.add(powerBig(new BigDecimal(t.step),new BigDecimal(memoryFactor)));
						numerator = numerator.add( powerBig(new BigDecimal(t.step),new BigDecimal(memoryFactor))
								.multiply(new BigDecimal(t.value)));
						
						
						//denominator= (float) (denominator + Math.pow(t.step, memoryFactor));
						//numerator =(float) (numerator + t.step.pow(memoryFactor,mc) *t.value);
						//Math.pow(t.step, memoryFactor)*t.value
						
						
						
					}
					
					
					
					Float auXdenominator = (float) 0; Float auXnumerator = (float) 0;
					Float auXmemory = memoryFactor.floatValue();
					for(TaskHistory t: history) {
						Float auxStep = t.step.floatValue();
						Float auXvalue = t.value.floatValue();
						auXdenominator= (float) (auXdenominator + Math.pow(auxStep, auXmemory));
						auXnumerator =(float) (auXnumerator + Math.pow(auxStep, auXmemory)*auXvalue);
						
					}
					Float auXcurrentUtility = (auXnumerator/auXdenominator);
					//System.out.println("auXcurrentUtility: "+ auXcurrentUtility+", currentUtility: "+currentUtility);
					currentUtility = (numerator.divide(denominator, mc).floatValue());
					if(auXcurrentUtility != currentUtility.floatValue()) {
						currentUtility = new Float(auXcurrentUtility);
					}
					
					//System.out.println("CU: "+currentUtility+" AuxCU: "+auXcurrentUtility);
					//currentUtility = (float) (Math.round(currentUtility * 100.0) / 100.0);
				}else{
					currentUtility = history.getLast().value;
				}
				
			}
	
		}
		
		@Override
		public String toString() {
			if(history.isEmpty()) {
				return "" + action + "=NA" ;
			}else {
				DecimalFormat df = new DecimalFormat("0.00");
				return "" + action + "=" + df.format(currentUtility) ;
			}	
		}
		
		//@Override
		public String toString1() {
			return "Task [ " + action + ", history=" + history + ", memoryFactor=" + memoryFactor
					+ ", initialUtility=" + initialUtility + ", currentUtility=" + currentUtility + ", realUtility="
					+ realUtility + ", cyclesWaited=" + cyclesWaited + "]";
		}
		

		@Override
		public int compareTo(Task o) {
			   return this.action.compareTo(o.action);
		}
	}
		
		//this might need to be erased

	//
	/******************************/
	/******* B: MAIN UTILS ********/
	/******************************/

    public static void main(String[] args) throws IOException { 
       
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = br.readLine();
		Agent agent = new Agent(line.split(" "));
		while(!(line=br.readLine()).startsWith("end")) {
			if(line.startsWith("TIK")) agent.decideAndAct();
			else agent.perceive(line);
		}
		System.out.println(agent.recharge());
		br.close();
	}
}
