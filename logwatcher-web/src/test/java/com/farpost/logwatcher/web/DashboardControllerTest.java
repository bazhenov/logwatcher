package com.farpost.logwatcher.web;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.storage.InMemoryLogStorage;
import com.farpost.logwatcher.storage.LogStorage;
import org.springframework.ui.ModelMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.farpost.logwatcher.TestSupport.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class DashboardControllerTest {

	DashboardController controller;
	LogStorage storage;
	private ModelMap map;

	@BeforeMethod
	public void setUp() {
		storage = new InMemoryLogStorage();
		controller = new DashboardController(storage);
		map = new ModelMap();
	}

	@Test
	public void dashboardShouldShowApplicationInfo() {
		entry().
			applicationId("frontend").
			message("Sweet sweet home!").
			saveMultipleTimesIn(storage, 4);
		controller.doDashboard(map);

		assertThat(map.get("infos"), instanceOf(List.class));
		List<ApplicationInfo> info = (List<ApplicationInfo>) map.get("infos");

		// Проверяем что показывается информация об одном приложении
		assertThat(info.size(), equalTo(1));

		// это приложение должно называтся "frontend"
		ApplicationInfo frontend = info.get(0);
		assertThat(frontend.getApplicationId(), equalTo("frontend"));
	}

	@Test
	public void dashboardShouldCorrectlyShowSpecifiedApplication() {
		entry().
			applicationId("foobar").
			message("Gimie Gimie!").
			saveMultipleTimesIn(storage, 3);
		controller.doDashboardWidget("foobar", map);

		assertThat(map.get("info"), instanceOf(ApplicationInfo.class));
		ApplicationInfo info = (ApplicationInfo) map.get("info");

		// проверяем что id приложения совпадает с тем что мы искали
		assertThat(info.getApplicationId(), equalTo("foobar"));

		// result должен содержать одну агррегированную запись
		List<AggregatedEntry> entries = info.getEntries();
		assertThat(entries.size(), equalTo(1));

		// об ошибке произошедшей три раза
		AggregatedEntry firstEntry = entries.get(0);
		assertThat(firstEntry.getCount(), equalTo(3));
		assertThat(firstEntry.getMessage(), equalTo("Gimie Gimie!"));
	}
}