package uk.co.jpawlak.java8exercises.exercises;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Arrays.asList;
import static uk.co.jpawlak.java8exercises.utils.Employees.allEmployees;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;

import uk.co.jpawlak.java8exercises.utils.Employee;

@SuppressWarnings("all")
public class Pack_4_Streams_Difficult {

    private static final List<Employee> EMPLOYEES = allEmployees();

    
    @Test
    public void exercise_1_findFirst() {
        // find whether there are two employees with the same first name and surname and return the name
        // the solution has to be a single statement, complexity O(n^2) is acceptable

    	EMPLOYEES.stream().forEach(item -> System.out.println(item.getFirstName() + " " + item.getSurname()));
    	System.out.println("#######################");
        
    	StopWatch stopWatch = new StopWatch();
        
    	
    	stopWatch.start();
    	String result = EMPLOYEES.stream()
        		.collect(Collectors.groupingBy(item -> item.getFirstName() + " " + item.getSurname(), LinkedHashMap::new, Collectors.counting()))
        		.entrySet().stream()
        		.peek(item -> System.out.println(item.getKey() + "  " + item.getValue()))
        		.filter(item -> item.getValue() > 1)
        		.peek(item -> System.out.println("> 1 : " + item.getKey() + "  " + item.getValue()))
        		.findFirst()
        		.map(Map.Entry::getKey)
        		.orElseGet(null);
    	stopWatch.stop();
    	
    	System.out.println("Time in Milliseconds: " + stopWatch.getTime());
    	
    	stopWatch.reset();
    	stopWatch.start();
    	
    	String result1 = EMPLOYEES.stream()
    			.filter(item -> EMPLOYEES.stream()
    					.filter(item2 -> item != item2 && item.getFirstName().equals(item2.getFirstName()) && item.getSurname().equals(item2.getSurname()))
    					.findFirst()
    					.isPresent())
    			.findFirst()
    			.map(i -> i.getFirstName() + " " + i.getSurname()).get(); 
    			
        stopWatch.stop();
        System.out.println("Time in Milliseconds: " + stopWatch.getTime());
        
        System.out.println(result);
        assertThat(result, sameBeanAs("Holly Davies"));
        assertThat(result1, sameBeanAs("Holly Davies"));
    }

    @Test
    public void exercise_2_groupingBy_counting() {
        // find the total number of groups of at least 5 employees living close to each other
        // consider all employees with the same 2 first characters of the home address post code a single group
        // you can collect to map and then stream over it, however the solution has to be a single statement

        long result = EMPLOYEES.stream()
        		.collect(Collectors.groupingBy(item -> item.getHomeAddress().getPostCode().substring(0, 2), Collectors.counting()))
        		.values()
        		.stream().filter(item -> item >= 5)
        		.count();
       

        //TODO write your code here

        assertThat(result, sameBeanAs(110L));
    }

    @Test
    public void exercise_3_flatMap() {
        // find the total number of all different home and correspondence addresses

        long result = EMPLOYEES.stream().flatMap(item -> Stream.of(item.getHomeAddress(), item.getCorrespondenceAddress().orElse(null)))
        								.filter(item -> item != null)
        								.distinct()
        								.count();

        //TODO write your code here

        assertThat(result, sameBeanAs(1820L));
    }

    @Test
    public void exercise_4_groupingBy_summingInt() {
        // find how much in total each company pays (annually) to their employees, order result by amount
        // you can convert the salaries to ints using BigDecimal#intValue method
        // you can collect to map and then stream over it, however the solution has to be a single statement

        DecimalFormat decimalFormat = new DecimalFormat("£#,###.00");
        List<String> result = EMPLOYEES.stream()
        		.collect(Collectors.groupingBy(Employee::getCompany, Collectors.reducing(0, item -> item.getSalary().intValue(), Integer::sum)))
        		.entrySet()
        		.stream()
        		.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        		.map(item -> item.getKey().getName() + " - " + decimalFormat.format(item.getValue()))
        		.collect(Collectors.toList());
        
        List<String> result1 = EMPLOYEES.stream()
        		.collect(Collectors.groupingBy(Employee::getCompany, Collectors.summingInt(emp -> emp.getSalary().intValue())))
        		.entrySet()
        		.stream()
        		.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        		.map(item -> item.getKey().getName() + " - " + decimalFormat.format(item.getValue()))
        		.collect(Collectors.toList());


        assertThat(result, sameBeanAs(asList(
                "Anglo American - £12,119,153.00",
                "HSBC - £11,469,144.00",
                "Royal Bank of Scotland Group - £11,127,807.00",
                "BP - £10,925,088.00",
                "AstraZeneca - £10,507,305.00",
                "HBOS - £10,428,819.00",
                "Royal Dutch Shell - £10,100,098.00",
                "Barclays plc - £10,071,534.00",
                "Vodafone Group - £10,029,401.00",
                "GlaxoSmithKline - £9,499,235.00"
        )));
    }

    @Test
    public void exercise_5_patternCompileSplitAsStream() {
        // count the instances of words and output a list of formatted strings
        // output the strings sorted lexicographically by name
        // you can use collect twice
        // as always, a single statement solution is expected
        // hint: look at Pattern.compile(regex).splitAsStream(string)

        String string = "dog" + "\n" + "bird" + "\n" + "cat" + "\n" + "cat" + "\n" + "dog" + "\n" + "cat";
        List<String> result = Pattern.compile("\n").splitAsStream(string)
        						.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        						.entrySet()
        						.stream()
        						.sorted(Map.Entry.comparingByKey())
        						.map(item -> item.getKey() + " - " + item.getValue())
        						.collect(Collectors.toList());


        assertThat(result, sameBeanAs(asList(
                "bird - 1",
                "cat - 3",
                "dog - 2"
        )));
    }

    @Test
    public void exercise_6_mapToLong() {
        // the rows and columns of the chess board are assigned arbitrary numbers (instead of letters and digits)
        // the value of the square is a multiplication of the numbers assigned to corresponding row and column
        // e.g. square A1 has a value 6,432 * 6,199 = 39,871,968
        // calculate the sum of values of all squares
    	
    	
    	
        int[] rows = {6432, 8997, 8500, 7036, 9395, 9372, 9715, 9634};
        int[] columns = {6199, 9519, 6745, 8864, 8788, 7322, 7341, 7395};
        
//        BiFunction<Long, int[], Long> mult = (val, array) -> {
//    		return Arrays.stream(array).mapToLong(item -> item * val).sum();
//    	};
//        long result = Arrays.stream(rows).mapToLong(Long::valueOf).map(item -> mult.apply(item, columns)).sum();
        
		long result = Arrays.stream(rows).mapToLong(Long::valueOf)
				.map(item -> Arrays.stream(columns).mapToLong(col -> col * item).sum()).sum();

        assertThat(result, sameBeanAs(4294973013L));
    }

    @Test
    public void exercise_7_randomLongs_concat_toArray() {
        // concatenate two random streams of numbers (seed is fixed for testing purposes),
        // then revert the sign of the negative ones
        // then sort them and pick 10 middle elements (hint: you can use skip and limit)
        // then do modulo 1000 (remainder of division by 1000)
        // and finally collect the result into an array
        LongStream longs = new Random(0).longs(10);
        IntStream ints = new Random(0).ints(10);
//        long[] l = longs.toArray();
//        int[] i = ints.toArray();
        int[] result = LongStream.concat(longs, ints.asLongStream())
        			.map(Math::abs)
        			.sorted()
        			.skip(5)
        			.limit(10)
        			.map(item -> item % 1000)
        			.mapToInt(item -> (int)item)
        			.toArray();


        assertThat(result, sameBeanAs(new long[] {106, 266, 402, 858, 313, 688, 303, 137, 766, 896}));
    }

}
