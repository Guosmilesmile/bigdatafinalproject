import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class LogCount {
	public static class WordCountMap extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		
		private final IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String[] split = line.split(" ");
			if("Exception".equals(split[0]) &&  "Codes:".equals(split[1])){
				word.set(line);
				context.write(word, one);
			}
			if("Exception".equals(split[0]) &&  "Subtype:".equals(split[1])){
				word.set(line);
				context.write(word, one);
			}
			/*StringTokenizer token = new StringTokenizer(line);
			while (token.hasMoreTokens()) {
				word.set(token.nextToken());
				context.write(word, one);
			}*/
		}
	}

	public static class WordCountReduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("HADOOP_USER_NAME", "root");
		Configuration conf = new Configuration();
		Job job = new Job(conf);
		job.setJarByClass(LogCount.class);
		job.setJobName("log");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setMapperClass(WordCountMap.class);
		job.setReducerClass(WordCountReduce.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		String inputpath = "hdfs://172.16.21.32:9000/user/root/input_wordcount";
		String outputpath = "hdfs://172.16.21.32:9000/user/root/out";
		FileInputFormat.addInputPath(job, new Path(args[0]));
		//FileInputFormat.addInputPath(job, new Path(inputpath));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		//FileOutputFormat.setOutputPath(job, new Path(outputpath));
		job.waitForCompletion(true);
	}
}
