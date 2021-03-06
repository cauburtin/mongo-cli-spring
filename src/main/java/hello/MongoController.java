package hello;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.plaf.basic.BasicScrollBarUI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteConcernException;
import com.mongodb.WriteResult;

@Controller
public class MongoController {
	
	@Autowired
	private DB db;
	
	
	@RequestMapping(value = "/mongo", method = RequestMethod.POST)
	public @ResponseBody Object mongo(
			@RequestParam(value="collection", required=false, defaultValue="foo") String collection,
			@RequestBody List<Object> message){
		
		String method = (String) message.remove(0);
		Object[] args = message.toArray();
		System.out.println(message.getClass());

		DBCollection coll = db.getCollection(collection);
		
		Class[] types = new Class[args.length];
		
    	for (int i = 0; i < args.length; i++) {
            if (args[i] == null) types[i] = DBObject.class;
            else {
            	types[i] = args[i].getClass();
                if (types[i].equals(LinkedHashMap.class)){ // convert Maps to DBObjects
                	args[i] = new BasicDBObject( (LinkedHashMap) args[i]);
                	types[i] = DBObject.class;
                }else if(types[i].equals(Boolean.class)){
                	types[i] = boolean.class;
                }
            }
        }
    	if(method.equals("insert") ){
			// wraps insert(DBObject) in insert(DBObject...) or insert(List<DBObject>) because it fails
			args[0] = Arrays.asList(args[0]);
			types[0] = List.class;
        }

		Object result = null;
		try {
			result = coll.getClass().getMethod(method, types).invoke(coll, args);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException | WriteConcernException e) {
			
			e.printStackTrace();
			return "Exception: "+e.getMessage();
		}
		
		if (method.equals("find")){
			DBCursor cursor = (DBCursor) result;
	        List<Object> list = new ArrayList<Object>();
	        while (cursor.hasNext())
	        	list.add(cursor.next());
	        cursor.close();
	        return list;
	        
		
		}
		
		return result;
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public @ResponseBody Object test(@RequestBody BasicDBObject message){
		return new BasicDBObject("test", message);
	}
	

}
