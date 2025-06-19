export type TimePickerType = "minutes" | "hours" | "seconds" | "12hours";
export type Period = "AM" | "PM";

/**
 * regular expression to check for valid hour format (01-23)
 */
export function isValidHour(value: string) {
	return /^(0[0-9]|1[0-9]|2[0-3])$/.test(value);
}

/**
 * regular expression to check for valid 12 hour format (01-12)
 */
export function isValid12Hour(value: string) {
	return /^(0[1-9]|1[0-2])$/.test(value);
}

/**
 * regular expression to check for valid minute format (00-59)
 */
export function isValidMinute(value: string) {
	return /^[0-5][0-9]$/.test(value);
}

/**
 * regular expression to check for valid second format (00-59)
 */
export function isValidSecond(value: string) {
	return /^[0-5][0-9]$/.test(value);
}

export type GetValidNumberConfig = {
	max: number;
	min?: number;
	loop?: boolean;
};

export function getValidNumber(
	value: string,
	{ max, min = 0, loop = false }: GetValidNumberConfig
) {
	let numericValue = parseInt(value, 10);

	if (!isNaN(numericValue)) {
		if (!loop) {
			if (numericValue > max) numericValue = max;
			if (numericValue < min) numericValue = min;
		} else {
			if (numericValue > max) numericValue = min;
			if (numericValue < min) numericValue = max;
		}
		return numericValue.toString().padStart(2, "0");
	}

	return "00";
}

export function getValidHour(value: string) {
	if (isValidHour(value)) return value;
	return getValidNumber(value, { max: 23 });
}

export function getValid12Hour(value: string) {
	if (isValid12Hour(value)) return value;
	return getValidNumber(value, { max: 12, min: 1 });
}

export function getValidMinute(value: string) {
	if (isValidMinute(value)) return value;
	return getValidNumber(value, { max: 59 });
}

export function getValidSecond(value: string) {
	if (isValidSecond(value)) return value;
	return getValidNumber(value, { max: 59 });
}

export type GetValidArrowNumberConfig = {
	min: number;
	max: number;
	step: number;
};

export function getValidArrowNumber(
	value: string,
	{ min, max, step }: GetValidArrowNumberConfig
) {
	let numericValue = parseInt(value, 10);
	if (!isNaN(numericValue)) {
		numericValue += step;
		return getValidNumber(String(numericValue), { min, max, loop: true });
	}
	return "00";
}

export function getValidArrowHour(value: string, step: number) {
	return getValidArrowNumber(value, { min: 0, max: 23, step });
}

export function getValidArrow12Hour(value: string, step: number) {
	return getValidArrowNumber(value, { min: 1, max: 12, step });
}

export function getValidArrowMinute(value: string, step: number) {
	return getValidArrowNumber(value, { min: 0, max: 59, step });
}

export function getValidArrowSecond(value: string, step: number) {
	return getValidArrowNumber(value, { min: 0, max: 59, step });
}

/**
 * handles value change of 12-hour input
 * 12:00 PM is 12:00
 * 12:00 AM is 00:00
 */
export function convert12HourTo24Hour(hour: string, period: Period) {
	if (period === "PM") {
		if (hour !== "12") {
			return String(parseInt(hour, 10) + 12).padStart(2, "0");
		} else {
			return hour;
		}
	} else if (period === "AM") {
		if (hour === "12") {
			return "00";
		} else {
			return hour;
		}
	}
	return hour;
}

/**
 * time is stored in the 24-hour format in the Date object
 * this function converts the time to 12-hour format
 */
export function convert24HourTo12Hour(hour: string): [string, Period] {
	const hourInt = parseInt(hour, 10);
	if (hourInt === 0) {
		return ["12", "AM"];
	} else if (hourInt < 12) {
		return [hour, "AM"];
	} else if (hourInt === 12) {
		return ["12", "PM"];
	} else {
		return [String(hourInt - 12).padStart(2, "0"), "PM"];
	}
}

/**
 * returns the current date in the format YYYY-MM-DD
 */
export function getCurrentDate() {
	const now = new Date();
	return now.toISOString().split("T")[0];
}

export function setDateByType(
	date: Date,
	value: string,
	type: TimePickerType,
	period?: Period
): Date {
	const newDate = new Date(date);

	switch (type) {
		case "minutes":
			newDate.setUTCMinutes(parseInt(value, 10));
			break;
		case "hours":
			newDate.setUTCHours(parseInt(value, 10));
			break;
		case "seconds":
			newDate.setUTCSeconds(parseInt(value, 10));
			break;
		case "12hours": {
			const hour = convert12HourTo24Hour(value, period || "AM");
			newDate.setUTCHours(parseInt(hour, 10));
			break;
		}
		default:
			break;
	}
	return newDate;
}

export function getDateByType(date: Date | undefined, type: TimePickerType) {
	if (!date) return "00";

	switch (type) {
		case "minutes":
			return date.getUTCMinutes().toString().padStart(2, "0");
		case "hours":
			return date.getUTCHours().toString().padStart(2, "0");
		case "seconds":
			return date.getUTCSeconds().toString().padStart(2, "0");
		case "12hours": {
			const [hour] = convert24HourTo12Hour(
				date.getUTCHours().toString().padStart(2, "0")
			);
			return hour;
		}
		default:
			return "00";
	}
}

export function getArrowByType(
	value: string,
	step: number,
	type: TimePickerType
) {
	switch (type) {
		case "minutes":
			return getValidArrowMinute(value, step);
		case "hours":
			return getValidArrowHour(value, step);
		case "seconds":
			return getValidArrowSecond(value, step);
		case "12hours":
			return getValidArrow12Hour(value, step);
		default:
			return "00";
	}
}
