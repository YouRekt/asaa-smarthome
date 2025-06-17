import { z } from "zod/v4";
import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export type SystemStatus = "stopped" | "starting" | "running" | "stopping";

export type Environment = {
	time: string;
	credits: number;
	timeDelta: number;
	maxPowerCapacity: number;
	currentPowerConsumption: number;
};

const AreaAttributes = ["temperature"] as const;

export const AreaAttributesSchema = z.enum(AreaAttributes);

export type AreaAttributes = z.infer<typeof AreaAttributesSchema>;

export type Area = {
	name: string;
	attributes: Record<AreaAttributes, number>;
};

export type AgentType = "sensor" | "appliance";

export type Agent = {
	aid: string;
	area: string;
	name: string;
	type: AgentType;
	status: AgentStatus;
};

export const TemplateIdSchema = z.enum([
	"Temperature Sensor",
	"Motion Sensor",
	"Smart Lightbulb",
	"AC Unit",
	"Coffee Machine",
	"Dishwasher",
	"Fridge",
]);

export type AgentTemplate = {
	name: z.infer<typeof TemplateIdSchema>;
	type: AgentType;
	description: string;
};

export const agentTemplates: AgentTemplate[] = [
	{
		name: "Temperature Sensor",
		type: "sensor",
		description: "Monitors room temperature",
	},
	{
		name: "Motion Sensor",
		type: "sensor",
		description: "Detects movement in the area",
	},
	{
		name: "Smart Lightbulb",
		type: "appliance",
		description: "Controllable LED light",
	},
	{
		name: "AC Unit",
		type: "appliance",
		description: "Air conditioning system",
	},
	{
		name: "Coffee Machine",
		type: "appliance",
		description: "Brews coffee on demand",
	},
	{
		name: "Dishwasher",
		type: "appliance",
		description: "Cleans dishes automatically",
	},
	{
		name: "Fridge",
		type: "appliance",
		description:
			"Refrigerates food and drinks. Orders groceries automatically",
	},
] as const;

export type Message = {
	aid: string;
	content: string;
	timestamp: string;
};

export type AgentStatus = "enabled" | "working" | "disabled";

type State = {
	systemStatus: SystemStatus;
	areas: Area[];
	agents: Agent[];
	messages: Message[];
	selectedArea: string;
	environment?: Environment;
};

type Actions = {
	setSystemStatus: (status: SystemStatus) => void;
	setSelectedArea: (area: string) => void;
	setAreas: (areas: Area[]) => void;
	setAgents: (agents: Agent[]) => void;
	addConfiguration: (areas: Area[], agents: Agent[]) => void;
	setEnvironment: (environment: Environment) => void;
	addMessage: (message: Message) => void;
	clearMessages: () => void;
};

// const defaultAreas: Area[] = [
// 	{
// 		name: "Kitchen",
// 		attributes: { temperature: 21.0 },
// 	},
// 	{
// 		name: "Living Room",
// 		attributes: { temperature: 22.0 },
// 	},
// 	{
// 		name: "Bedroom",
// 		attributes: { temperature: 20.0 },
// 	},
// 	{
// 		name: "Bathroom",
// 		attributes: { temperature: 23.0 },
// 	},
// ];

// const defaultAgents: Agent[] = [
// 	{
// 		aid: "0",
// 		area: "Kitchen",
// 		name: "Temperature Sensor",
// 		type: "sensor",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "1",
// 		area: "Kitchen",
// 		name: "Motion Sensor",
// 		type: "sensor",
// 		status: "disabled",
// 	},
// 	{
// 		aid: "2",
// 		area: "Kitchen",
// 		name: "Smart Lightbulb",
// 		type: "appliance",
// 		status: "working",
// 	},
// 	{
// 		aid: "3",
// 		area: "Kitchen",
// 		name: "AC",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "4",
// 		area: "Kitchen",
// 		name: "Fridge",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "5",
// 		area: "Kitchen",
// 		name: "Coffe Machine",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "6",
// 		area: "Kitchen",
// 		name: "Dishwasher",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "7",
// 		area: "Kitchen",
// 		name: "Oven",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "8",
// 		area: "Kitchen",
// 		name: "Washing Machine",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "9",
// 		area: "Kitchen",
// 		name: "Dryer",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "10",
// 		area: "Kitchen",
// 		name: "Smart Speaker",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "11",
// 		area: "Kitchen",
// 		name: "Smart Thermostat",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "12",
// 		area: "Kitchen",
// 		name: "Smart TV",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// 	{
// 		aid: "13",
// 		area: "Kitchen",
// 		name: "Smart Vacuum Cleaner",
// 		type: "appliance",
// 		status: "enabled",
// 	},
// ];

// const defaultMessages: Message[] = [
// 	{
// 		aid: "0",
// 		timestamp: new Date().toISOString(),
// 		content: "Temperature is 21°C",
// 	},
// 	{
// 		aid: "1",
// 		timestamp: new Date().toISOString(),
// 		content: "Motion detected in the kitchen",
// 	},
// 	{
// 		aid: "2",
// 		timestamp: new Date().toISOString(),
// 		content: "Smart lightbulb turned on",
// 	},
// 	{
// 		aid: "3",
// 		timestamp: new Date().toISOString(),
// 		content: "AC set to 22°C",
// 	},
// 	{
// 		aid: "4",
// 		timestamp: new Date().toISOString(),
// 		content: "Fridge temperature is stable",
// 	},
// 	{
// 		aid: "5",
// 		timestamp: new Date().toISOString(),
// 		content: "Coffee machine started brewing",
// 	},
// 	{
// 		aid: "6",
// 		timestamp: new Date().toISOString(),
// 		content: "Dishwasher cycle completed",
// 	},
// 	{
// 		aid: "7",
// 		timestamp: new Date().toISOString(),
// 		content: "Oven preheated to 180°C",
// 	},
// 	{
// 		aid: "8",
// 		timestamp: new Date().toISOString(),
// 		content: "Washing machine finished cycle",
// 	},
// 	{
// 		aid: "9",
// 		timestamp: new Date().toISOString(),
// 		content: "Dryer completed drying clothes",
// 	},
// 	{
// 		aid: "10",
// 		timestamp: new Date().toISOString(),
// 		content: "Smart speaker playing music",
// 	},
// 	{
// 		aid: "11",
// 		timestamp: new Date().toISOString(),
// 		content: "Smart thermostat adjusted to 20°C",
// 	},
// 	{
// 		aid: "12",
// 		timestamp: new Date().toISOString(),
// 		content: "Smart TV turned on",
// 	},
// 	{
// 		aid: "13",
// 		timestamp: new Date().toISOString(),
// 		content:
// 			"Lorem ipsum dolor sit amet consectetur adipisicing elit. Necessitatibus odio sit eos quod, earum pariatur dolores iure, quas repellendus velit, sapiente aliquam? Eum, itaque ea fuga beatae vero in porro. Lorem ipsum dolor sit amet consectetur adipisicing elit. Necessitatibus odio sit eos quod, earum pariatur dolores iure, quas repellendus velit, sapiente aliquam? Eum, itaque ea fuga beatae vero in porro. Lorem ipsum dolor sit amet consectetur adipisicing elit. Necessitatibus odio sit eos quod, earum pariatur dolores iure, quas repellendus velit, sapiente aliquam? Eum, itaque ea fuga beatae vero in porro.",
// 	},
// ];

export const useStore = create<State & Actions>()(
	immer((set) => ({
		systemStatus: "stopped",
		areas: [],
		agents: [],
		messages: [],
		selectedArea: "Kitchen",
		setSystemStatus: (status) =>
			set((state) => {
				state.systemStatus = status;
			}),
		setSelectedArea: (area) =>
			set((state) => {
				state.selectedArea = area;
			}),
		setAreas: (areas) =>
			set((state) => {
				state.areas = areas;
			}),
		setAgents: (agents) =>
			set((state) => {
				state.agents = agents;
			}),
		addConfiguration: (areas, agents) =>
			set((state) => {
				state.areas = areas;
				state.agents = agents;
			}),
		setEnvironment: (environment) =>
			set((state) => {
				state.environment = environment;
			}),
		addMessage: (message) =>
			set((state) => {
				state.messages.push(message);
			}),
		clearMessages: () =>
			set((state) => {
				state.messages = [];
			}),
	}))
);
