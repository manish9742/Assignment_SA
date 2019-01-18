package com.sa.assignment.context.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sa.assignment.exception.LoopsNotAllowedAcception;
import com.sa.assignment.orm.Planet;
import com.sa.assignment.orm.Route;
import com.sa.assignment.repository.PlanetRepository;
import com.sa.assignment.repository.RouteRepository;


 
@Component
@SuppressWarnings("deprecation")
public class DataLoaderImpl implements DataLoader {

	@Autowired
	PlanetRepository planetRepo;

	@Autowired
	RouteRepository routesRepo;

	@Value(value = "${xlsx.data.file.location}")
	private String XLSX_DATA_FILE;
	
	static final Logger LOG = org.apache.log4j.Logger.getLogger(DataLoaderImpl.class);

	
	 
	@EventListener
	public void onApplicationEvent(ContextStartedEvent startEvent) {
		readXlsDataFile();
	}
 
	@Override
	public void readXlsDataFile() {
		try {
			Workbook workbook = createWorkBook();
			processPlanetSheet(workbook);
			processRoutesSheet(workbook);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	 

	
	private void processPlanetSheet(Workbook workbook) throws FileNotFoundException, IOException {
		Sheet planetsSheet = getSheet(workbook, 0);
		Iterator<Row> iterator = planetsSheet.iterator();
		while (iterator.hasNext()) {
			Row currentRow = iterator.next();
			Cell planetIdCell = currentRow.getCell(0);
			Cell planetNameCell = currentRow.getCell(1);
			if ((planetIdCell.getCellTypeEnum() == CellType.STRING)
					&& (planetNameCell.getCellTypeEnum() == CellType.STRING)) {
				savePlanets(planetIdCell.getStringCellValue(), planetNameCell.getStringCellValue());
			}
		}
	}

	 
	
	private void processRoutesSheet(Workbook workbook) throws FileNotFoundException, IOException {
		Sheet planetSheet = getSheet(workbook, 1);
		Iterator<Row> it = planetSheet.iterator();
		while (it.hasNext()) {
			Row row = it.next();
			Cell idCell = row.getCell(0);
			Cell sourceCell = row.getCell(1);
			Cell destCell = row.getCell(2);
			Cell weightCell = row.getCell(3);
			short routeID = 0;
			String source = " ";
			String destination = " ";
			float distance = 0.0f;
			if (idCell.getCellTypeEnum() == CellType.NUMERIC) {
				routeID = (short) idCell.getNumericCellValue();
			} else {
				continue;
			}
			if (sourceCell.getCellTypeEnum() == CellType.STRING) {
				source = sourceCell.getStringCellValue();
			}
			if (destCell.getCellTypeEnum() == CellType.STRING) {
				destination = destCell.getStringCellValue();
			}
			if (weightCell.getCellTypeEnum() == CellType.NUMERIC) {
				distance = (float) weightCell.getNumericCellValue();
			}
			saveRoute(routeID, source, destination, distance);
		}
	}

	private void saveRoute(short routeID, String source, String destination, float distance) {
		try {
			if (source != destination) {
				persistRoute(routeID, source, destination, distance);
			}
		} catch (LoopsNotAllowedAcception e) {
			LOG.info(e.getLocalizedMessage() + "for route id=" + routeID + "got  " + source + " and " + destination);
		}
	}
	
	 

	private Workbook createWorkBook() throws FileNotFoundException, IOException {
		FileInputStream xlsFile = new FileInputStream(new File(XLSX_DATA_FILE));
		return new XSSFWorkbook(xlsFile);
	}
	
	 

	private Sheet getSheet(Workbook workBook, int index) throws FileNotFoundException, IOException {
		return workBook.getSheetAt(index);
	}
	
	 

	private void savePlanets(String node, String descr) {
		if (!node.contains("Node")) {
			planetRepo.save(new Planet(node, descr));
			LOG.info("Saved Planet ==> {Planet Node: " + node + "   Name: " + descr + "}");
		}
	}

	
	 
	private void persistRoute(Short routeId, String origin, String planetDest, float distance)
			throws LoopsNotAllowedAcception {
		Planet source = planetRepo.findOne(origin);
		Planet dest = planetRepo.findOne(planetDest);
		if ((source != null) && (dest != null)) {
			routesRepo.save(new Route(routeId, source, dest, distance));
			LOG.info("Saved Route ==> {Route ID: " + routeId + "  Source : " + source.getPlanetID()+ " Dest : " + dest.getPlanetID() + " Distance " + distance + "}");
		}
	}
}
