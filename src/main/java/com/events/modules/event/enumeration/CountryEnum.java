package com.events.modules.event.enumeration;

import lombok.Getter;

@Getter
public enum CountryEnum {
    USA("USA", "US"),;

        private final String name;
        private final String code;

        CountryEnum(String name, String code){
            this.name = name;
            this.code = code;
        }

        public static CountryEnum getByCode(String code){
            for(CountryEnum countryEnum : CountryEnum.values()){
                if(countryEnum.getCode().equalsIgnoreCase(code)){
                    return countryEnum;
                }
            }
            return null;
        }

        public static CountryEnum getByName(String name){
            for(CountryEnum countryEnum : CountryEnum.values()){
                if(countryEnum.getName().equalsIgnoreCase(name)){
                    return countryEnum;
                }
            }
            return null;
        }
}
