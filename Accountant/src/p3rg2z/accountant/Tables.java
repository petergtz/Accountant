/*
   Copyright 2013 Peter Goetz

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package p3rg2z.accountant;

import android.provider.BaseColumns;


public class Tables {

    public static final String BOOKINGS = "bookings";
    public static final String ACCOUNTS = "accounts";
    
    public static final class Bookings implements BaseColumns {
        public static final String AMOUNT = "amount";
        public static final String TEXT = "text";
        public static final String SOURCE = "source";
        public static final String DEST = "dest";
        public static final String DATE = "datetime";
        
        public static final int AMOUNT_INDEX = 0;
        public static final int TEXT_INDEX = 1;
        public static final int SOURCE_INDEX = 2;
        public static final int DEST_INDEX = 3;
        public static final int DATETIME_INDEX = 4;
        
        private Bookings() {}
    }

    public static enum AccountType {
        BANK, INCOME_SOURCE, DEST_CATEGORY
    }

    public static final class Accounts implements BaseColumns {
        
        public static final String NAME = "name";
        public static final String TYPE = "type";
        
        public static final int NAME_INDEX = 1;
        public static final int TYPE_INDEX = 2;
        
        private Accounts() {}
    }
}
