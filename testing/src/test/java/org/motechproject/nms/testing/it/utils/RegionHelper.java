package org.motechproject.nms.testing.it.utils;

import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;

public class RegionHelper {
    private LanguageDataService languageDataService;
    private CircleDataService circleDataService;
    private DistrictDataService districtDataService;
    private StateDataService stateDataService;

    public RegionHelper(LanguageDataService languageDataService,
                              CircleDataService circleDataService,
                              StateDataService stateDataService,
                              DistrictDataService districtDataService) {

        this.languageDataService = languageDataService;
        this.circleDataService = circleDataService;
        this.districtDataService = districtDataService;
        this.stateDataService = stateDataService;
    }

    public Circle delhiCircle() {
        Circle c = circleDataService.findByName("DE");

        if (c == null) {
            c = circleDataService.create(new Circle("DE"));
            c.getStates().add(delhiState());
            c.setDefaultLanguage(hindiLanguage());
            circleDataService.update(c);
        }

        return c;
    }

    public Circle karnatakaCircle() {
        Circle c = circleDataService.findByName("KA");

        if (c == null) {
            c = circleDataService.create(new Circle("KA"));
            c.getStates().add(karnatakaState());
            circleDataService.update(c);
        }

        return c;
    }

    public State delhiState() {
        State s = stateDataService.findByCode(1l);

        if (s == null) {
            s = new State();
            s.setName("National Capital Territory of Delhi");
            s.setCode(1L);
            stateDataService.create(s);
        }

        return s;
    }


    public State karnatakaState() {
        State s = stateDataService.findByCode(2l);

        if (s == null) {
            s = new State();
            s.setName("Karnataka");
            s.setCode(2L);
            stateDataService.create(s);
        }

        return s;
    }


    public District newDelhiDistrict() {
        District d = districtDataService.findByCode(1L);

        if (d == null) {
            d = new District();
            d.setName("New Delhi");
            d.setRegionalName("New Delhi");
            d.setCode(1L);
            d.setState(delhiState());
            d.setLanguage(hindiLanguage());
            districtDataService.create(d);
        }

        return d;
    }


    public District southDelhiDistrict() {
        District d = districtDataService.findByCode(5L);

        if (d == null) {
            d = new District();
            d.setName("South Delhi");
            d.setRegionalName("South Delhi");
            d.setCode(5L);
            d.setState(delhiState());
            d.setLanguage(punjabiLanguage());
            districtDataService.create(d);
        }

        return d;
    }


    public District bangaloreDistrict() {
        District d = districtDataService.findByCode(4L);

        if (d == null) {
            d = new District();
            d.setName("Bengaluru");
            d.setRegionalName("Bengaluru");
            d.setCode(4L);
            d.setState(karnatakaState());
            d.setLanguage(tamilLanguage());
            districtDataService.create(d);
        }

        return d;
    }


    public District mysuruDistrict() {
        District d = districtDataService.findByCode(2L);

        if (d == null) {
            d = new District();
            d.setName("Mysuru");
            d.setRegionalName("Mysuru");
            d.setCode(2L);
            d.setState(karnatakaState());
            d.setLanguage(kannadaLanguage());
            districtDataService.create(d);
        }

        return d;
    }


    public Language tamilLanguage() {
        Language l = languageDataService.findByName("Tamil");

        if (l == null) {
            l = languageDataService.create(new Language("ta", "Tamil"));
        }

        return l;
    }


    public Language kannadaLanguage() {
        Language l = languageDataService.findByName("Kannada");

        if (l == null) {
            l = languageDataService.create(new Language("kn", "Kannada"));
        }

        return l;
    }


    public Language punjabiLanguage() {
        Language l = languageDataService.findByName("Punjabi");

        if (l == null) {
            l = languageDataService.create(new Language("pa", "Punjabi"));
        }

        return l;
    }


    public Language hindiLanguage() {
        Language l = languageDataService.findByName("Hindi");

        if (l == null) {
            l = languageDataService.create(new Language("hi", "Hindi"));
        }

        return l;
    }

    public String airtelOperator()
    {
        return "A";
    }

    public static State createState(Long code, String name) {
        State state = new State();
        state.setCode(code);
        state.setName(name);
        return state;
    }

    public static District createDistrict(State state, Long code, String name) {
        return createDistrict(state, code, name, null);
    }

    public static District createDistrict(State state, Long code, String name, Language language) {
        District district = new District();
        district.setState(state);
        district.setCode(code);
        district.setName(name);
        district.setRegionalName(regionalName(name));
        district.setLanguage(language);
        return district;
    }

    public static Language createLanguage(String code, String name) {
        return new Language(code, name);
    }

    public static Taluka createTaluka(District district, String code, String name, int identity) {
        Taluka taluka = new Taluka();
        taluka.setDistrict(district);
        taluka.setCode(code);
        taluka.setName(name);
        taluka.setRegionalName(regionalName(name));
        taluka.setIdentity(identity);
        return taluka;
    }

    public static HealthBlock createHealthBlock(Taluka taluka, Long code, String name, String hq) {
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setTaluka(taluka);
        healthBlock.setCode(code);
        healthBlock.setName(name);
        healthBlock.setRegionalName(regionalName(name));
        healthBlock.setHq(hq);
        return healthBlock;
    }

    public static Village createVillage(Taluka taluka, Long svid, Long vcode, String name) {
        Village village = new Village();
        village.setTaluka(taluka);
        village.setSvid(svid);
        village.setVcode(vcode);
        village.setName(name);
        village.setRegionalName(regionalName(name));
        return village;
    }

    public static HealthFacility createHealthFacility(HealthBlock healthBlock, Long code, String name, HealthFacilityType type) {
        HealthFacility healthFacility = new HealthFacility();
        healthFacility.setHealthBlock(healthBlock);
        healthFacility.setCode(code);
        healthFacility.setName(name);
        healthFacility.setRegionalName(regionalName(name));
        healthFacility.setHealthFacilityType(type);
        return healthFacility;
    }

    public static HealthFacilityType createHealthFacilityType(String name, Long code) {
        HealthFacilityType healthFacilityType = new HealthFacilityType();
        healthFacilityType.setName(name);
        healthFacilityType.setCode(code);
        return healthFacilityType;
    }

    public static Language createLanguage(String code, String name, Circle circle, boolean defaultForCircle, District... districts) {
        Language language = new Language();
        language.setCode(code);
        language.setName(name);
        for (District district : districts) {
            district.setLanguage(language);
        }
        if (defaultForCircle) {
            circle.setDefaultLanguage(language);
        }

        return language;
    }

    public static Circle createCircle(String name) {
        return new Circle(name);
    }

    public static String regionalName(String name) {
        return String.format("regional name of %s", name);
    }

}
