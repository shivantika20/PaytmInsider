import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.restassured.RestAssured.given;
// Run the main method
public class UpcomingMovies {

    final JsonPath js;
    final int count;

    UpcomingMovies(String URL) {
        RestAssured.baseURI = URL;
        String response = given().when().get("v2/movies/upcoming").then().assertThat().statusCode(200)
                .extract().response().asString();
        js = new JsonPath(response);

        //Count of upcoming movie data
        count = js.getInt("upcomingMovieData.size()");
        System.out.println(count);
    }

    @Test
    public void statusCodeCheck() {
        // Movie Release
        for (int i = 0; i < count - 1; i++) {
            String releaseDate = js.get("upcomingMovieData[" + i + "].releaseDate");
            if (releaseDate.equals(""))
                continue;
            Date date1;
            try {
                date1 = new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                // System.out.println(df.format(date1));
                Date date = new Date();
                String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
                //System.out.println(modifiedDate);
                if (releaseDate.compareTo(modifiedDate) > 0) ;
                System.out.println(releaseDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void movieURL() {

        int movieURLCount = 0;
        for (int i = 0; i < count; i++) {
            String movieURL = js.getString("upcomingMovieData[" + i + "].moviePosterUrl");
            if (movieURL.substring(movieURL.length() - 4).equalsIgnoreCase(".jpg"))
                System.out.println(movieURL + " \t " + (movieURLCount++) + "\n ");
        }
    }


    // 2nd method to check statuscode with assertion
    @Test
    public void checkStatusCode() {
        RestAssured.baseURI = "https://apiproxy.paytm.com";
        RequestSpecification httpRequest = RestAssured.given();
        Response response = httpRequest.get("v2/movies/upcoming");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode /*actual value*/, 200 /*expected value*/);
        System.out.println(statusCode);
    }

    @Test
    public void movieCode() {

        int movieCodeCount = 0;
        HashMap<String, Integer> uniqueMovieCode = new HashMap<String, Integer>();
        for (int i = 0; i < count; i++) {
            String movieCode = js.getString("upcomingMovieData[" + i + "].paytmMovieCode");
            if (uniqueMovieCode.containsKey(movieCode)) {

                uniqueMovieCode.replace(movieCode, uniqueMovieCode.get(movieCode) + 1);
            } else {
                uniqueMovieCode.put(movieCode, 1);
            }
        }
        for (int i = 0; i < count; i++) {
            String movieCode = js.getString("upcomingMovieData[" + i + "].paytmMovieCode");
            if (uniqueMovieCode.get(movieCode) == 1) {
                System.out.println(movieCode + " \t " + (movieCodeCount++) + "\n ");
            }
        }
    }

    @Test
    public void movieLanguage() {

        int movieLanguageCount = 0;
        for (int i = 0; i < count; i++) {
            String movieLanguage = js.getString("upcomingMovieData[" + i + "].language");
            if (!movieLanguage.contains(",")) {
                System.out.println(movieLanguage + " \t " + (movieLanguageCount++) + "\n ");
            }
        }
    }

    @Test
    public void contentAvailable() {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet ("MovieData");
        Map< String, String> data = new TreeMap<String, String>();

        for (int i = 0; i < count; i++) {
            String contentAvailable = js.getString("upcomingMovieData[" + i + "].isContentAvailable");
            if (contentAvailable.equalsIgnoreCase("0")) {
                data.put(Integer.toString(i),js.getString("upcomingMovieData[" + i + "].movieTitle"));
                System.out.println(js.getString("upcomingMovieData[" + i + "].movieTitle"));
            }
        }
        Set<String> keyset = data.keySet();
        int rowNum = 0;
        int cellNum = 0;
        for(String key : keyset) {
          //  System.out.println(rowNum+"\t"+cellNum);
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(data.get(key));
        }
        try{
            FileOutputStream out = new FileOutputStream(new File("/Users/shivantika.t/Downloads/movieData.xlsx"));
            workbook.write(out);
            out.close();
            System.out.println("File created in path : "+("/Users/shivantika.t/Downloads/movieData.xlsx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UpcomingMovies um = new UpcomingMovies("https://apiproxy.paytm.com");
        um.statusCodeCheck();
        um.movieURL();
        um.contentAvailable();
        um.movieCode();
        um.movieLanguage();
    }
}


