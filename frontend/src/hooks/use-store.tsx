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
	performedTasks: number;
	errorTasks: number;
	humanLocation: string | null;
};

const AreaAttributes = ["temperature"] as const;

export const AreaAttributesSchema = z.enum(AreaAttributes);

export type AreaAttributes = z.infer<typeof AreaAttributesSchema>;

export type Area = {
	name: string;
	attributes: Record<AreaAttributes, number>;
};

export type AgentType = "sensor" | "appliance";

export type AgentStatus = {
	isEnabled: boolean;
	isWorking: boolean;
	isTaskInterruptible?: boolean;
	isTaskResumable?: boolean;
	idleDraw?: number;
	activeDraw?: number;
	priority?: number;
};

export type Agent = {
	aid: string;
	area: string;
	name: string;
	type: AgentType;
	status: AgentStatus;
};

// New Error type
export type AgentError = {
	timestamp: string;
	sender: string;
	message: string;
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

export const performatives = [
	"ACCEPT_PROPOSAL",
	"AGREE",
	"CANCEL",
	"CFP",
	"CONFIRM",
	"DISCONFIRM",
	"FAILURE",
	"INFORM",
	"INFORM_IF",
	"INFORM_REF",
	"NOT_UNDERSTOOD",
	"PROPOSE",
	"QUERY_IF",
	"QUERY_REF",
	"REFUSE",
	"REJECT_PROPOSAL",
	"REQUEST",
	"REQUEST_WHEN",
	"REQUEST_WHENEVER",
	"SUBSCRIBE",
	"PROXY",
	"PROPAGATE",
	"UNKNOWN",
] as const;

export const performativeSchema = z.enum(performatives);

export type Performative = z.infer<typeof performativeSchema>;
type AID = string;

export type Message = {
	sender: AID;
	dtoSender: AID;
	receiver: AID[];
	content: string;
	timestamp: string;
	performative: Performative;
	conversationId: string;
	outgoing: boolean;
};

export const getIncomingMessages = (
	agentAid: AID,
	messages: Message[]
): Message[] => {
	return messages.filter(
		(message) =>
			message.receiver.includes(agentAid) &&
			message.dtoSender === agentAid &&
			!message.outgoing
	);
};

export const getOutgoingMessages = (
	agentAid: AID,
	messages: Message[]
): Message[] => {
	return messages.filter(
		(message) => message.dtoSender === agentAid && message.outgoing
	);
};

// New helper function for agent errors
export const getAgentErrors = (
	agentAid: AID,
	errors: AgentError[]
): AgentError[] => {
	return errors.filter((error) => error.sender === agentAid);
};

type State = {
	systemStatus: SystemStatus;
	areas: Area[];
	agents: Agent[];
	messages: Message[];
	errors: AgentError[]; // New errors array
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
	updateAgentStatus: (agentStatus: AgentStatus, aid: string) => void;
	updateAreaAttributes: (
		areaName: string,
		attributes: Partial<Record<AreaAttributes, number>>
	) => void;
	addError: (error: AgentError) => void; // New error action
	clearErrors: () => void; // New clear errors action
	resetAllState: () => void;
};

const initialState: State = {
	systemStatus: "stopped",
	areas: [],
	agents: [],
	messages: [],
	errors: [], // Initialize errors
	selectedArea: "Kitchen",
	environment: undefined,
};

export const useStore = create<State & Actions>()(
	immer((set) => ({
		...initialState,
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
		updateAgentStatus: (agentStatus, aid) =>
			set((state) => {
				const agent = state.agents.find((a) => a.aid === aid);
				if (agent) {
					agent.status = {
						...agent.status,
						...agentStatus,
					};
				}
			}),
		updateAreaAttributes: (areaName, attributes) =>
			set((state) => {
				const area = state.areas.find(
					(a) => a.name.toLowerCase() === areaName.toLowerCase()
				);
				if (area) {
					Object.entries(attributes).forEach(([key, value]) => {
						if (value !== undefined) {
							area.attributes[key as AreaAttributes] = value;
						}
					});
				}
			}),
		addError: (error) =>
			set((state) => {
				state.errors.push(error);
			}),
		clearErrors: () =>
			set((state) => {
				state.errors = [];
			}),
		resetAllState: () =>
			set((state) => {
				// Reset all state to initial values
				state.systemStatus = "stopped";
				state.areas = [];
				state.agents = [];
				state.messages = [];
				state.errors = []; // Reset errors
				state.selectedArea = "Kitchen";
				state.environment = undefined;
			}),
	}))
);
