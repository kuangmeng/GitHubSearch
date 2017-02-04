package hitamigos.githubsearch.data;
import android.content.Context;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {


        //private static List<ColorWrapper> colorWrappers = new ArrayList<>();

        public interface OnFindResultsListener{

                void onResults(List<Suggestion> results);
        }

        public static List<Suggestion> getHistory(Context context, int count){

                initColorWrapperList(context);

                List<Suggestion> suggestionList = new ArrayList<>();

                Suggestion suggestion;
                for(int i=0; i<count; i++){
                     //   suggestion = new Suggestion(colorWrappers.get(i));
                    //    suggestion.setIsHistory(true);
                     //   suggestionList.add(suggestion);
                }

                return suggestionList;
        }

        public static void find(Context context, String query, final OnFindResultsListener listener){

                initColorWrapperList(context);

                new Filter(){

                        @Override
                        protected FilterResults performFiltering(CharSequence constraint) {


                                List<Suggestion> suggestionList = new ArrayList<>();

                                if (!(constraint == null || constraint.length() == 0)) {
//
//                                        for(ColorWrapper color: colorWrappers){
//                                                if(color.getName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
//                                                        suggestionList.add(new Suggestion(color));
//                                        }

                                }

                                FilterResults results = new FilterResults();
                                results.values = suggestionList;
                                results.count = suggestionList.size();

                                return results;
                        }

                        @Override
                        protected void publishResults(CharSequence constraint, FilterResults results) {

                                if(listener!=null)
                                        listener.onResults((List<Suggestion>)results.values);
                        }
                }.filter(query);

        }

        private static void initColorWrapperList(Context context){

             //   if(colorWrappers.isEmpty()) {

                      //  String jsonString = loadJson(context);
                     //   colorWrappers = deserializeColors(jsonString);
          //      }
        }

//        private static String loadJson(Context context) {
//
//                String jsonString;
//
//                try {
//                        InputStream is = context.getAssets().open(COLORS_FILE_NAME);
//                        int size = is.available();
//                        byte[] buffer = new byte[size];
//                        is.read(buffer);
//                        is.close();
//                        jsonString = new String(buffer, "UTF-8");
//                } catch (IOException ex) {
//                        ex.printStackTrace();
//                        return null;
//                }
//
//                return jsonString;
//        }

//        private static List<ColorWrapper> deserializeColors(String jsonString){
//
//                Gson gson = new Gson();
//
//                Type collectionType = new TypeToken<List<ColorWrapper>>() {}.getType();
//                return gson.fromJson(jsonString, collectionType);
//        }

}