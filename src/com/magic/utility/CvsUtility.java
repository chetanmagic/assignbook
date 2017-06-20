package com.magic.utility;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.magic.assignbook.GradePONumber;

public class CvsUtility {

	static Logger log = Logger.getLogger(CvsUtility.class);

	private static final String[] FILE_HEADER_MAPPING = { "grade", "ponumber", "tenant" };

	// Student attributes
	private static final String GRADE = "grade";
	private static final String PONUMBER = "ponumber";
	private static final String TENANT = "tenant";

	public static List<GradePONumber> readCsvFile(String fileName) {

		FileReader fileReader = null;

		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING);

		List<GradePONumber> gradePONumberList = new ArrayList<GradePONumber>();

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader, csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			// Read the CSV file records starting from the second record to skip
			// the header
			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = (CSVRecord) csvRecords.get(i);
				// Create a new student object and fill his data
				GradePONumber gradePONumber = new GradePONumber(record.get(GRADE), record.get(PONUMBER),
						record.get(TENANT));
				gradePONumberList.add(gradePONumber);
			}

		} catch (Exception e) {
			log.error("Error in CsvFileReader !!!");
			log.error(e);
		} finally {
			try {
				fileReader.close();
				csvFileParser.close();
			} catch (IOException e) {
				log.error("Error while closing fileReader/csvFileParser !!!");
				log.error(e);
			}
		}
		return gradePONumberList;
	}
}
