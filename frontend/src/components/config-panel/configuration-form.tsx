import ConfigurationFormAgents from "@/components/config-panel/configuration-form-agents";
import { CounterInput } from "@/components/config-panel/counter-input";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardFooter,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	FormControl,
	FormDescription,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { TimePicker } from "@/components/ui/time-picker";
import {
	agentTemplates,
	AreaAttributesSchema,
	TemplateIdSchema,
	useStore,
	type Agent,
	type AgentStatus,
	type Area,
} from "@/hooks/use-store";
import { X } from "lucide-react";
import { useFormContext, type UseFieldArrayReturn } from "react-hook-form";
import { z } from "zod/v4";

export const ConfigurationFormSchema = z.object({
	simulationStartTime: z.date(),
	maxPowerCapacity: z.number().int().min(0),
	credits: z.number().int().min(0),
	areas: z
		.array(
			z.object({
				name: z.string().min(1, {
					message: "Area name is required",
				}),
				attributes: z.record(AreaAttributesSchema, z.number()),
				agents: z
					.array(
						z.object({
							templateId: TemplateIdSchema,
							count: z.number().min(0),
						})
					)
					.refine(
						(agents) => agents.some((agent) => agent.count > 0),
						{
							message: "At least one agent is required",
						}
					),
			})
		)
		.check((ctx) => {
			if (ctx.value.length > 1) {
				const nameToIndices = new Map<string, number[]>();

				// Group areas by lowercase name to find duplicates
				ctx.value.forEach((area, index) => {
					const normalizedName = area.name.toLowerCase().trim();
					if (!nameToIndices.has(normalizedName)) {
						nameToIndices.set(normalizedName, []);
					}
					nameToIndices.get(normalizedName)!.push(index);
				});

				// Add errors for each duplicate
				for (const [, indices] of nameToIndices.entries()) {
					if (indices.length > 1) {
						// Add error to each duplicate field
						indices.forEach((index) => {
							ctx.issues.push({
								code: "custom",
								message: "Area names must be unique",
								path: [index, "name"], // This will target areas[index].name
								input: ctx.value[index].name,
							});
						});
					}
				}
			}
		}),
});

export type ConfigurationFormValues = z.infer<typeof ConfigurationFormSchema>;

const ConfigurationForm = ({
	fieldArray,
}: {
	fieldArray: UseFieldArrayReturn<ConfigurationFormValues>;
}) => {
	const form = useFormContext<ConfigurationFormValues>();
	const { addConfiguration } = useStore();

	const { fields: areas } = fieldArray;

	function onSubmit(data: ConfigurationFormValues) {
		// Transform form data to store format
		const transformedAreas: Area[] = data.areas.map((area) => ({
			name: area.name,
			attributes: area.attributes,
		}));

		// First pass: count total agents per template
		const templateCounts = new Map<string, number>();
		data.areas.forEach((area) => {
			area.agents
				.filter((agent) => agent.count > 0)
				.forEach((agent) => {
					const template = agentTemplates.find(
						(t) => t.name === agent.templateId
					);
					if (template) {
						const currentCount =
							templateCounts.get(template.name) || 0;
						templateCounts.set(
							template.name,
							currentCount + agent.count
						);
					}
				});
		});

		// Second pass: create agents with proper indexing
		const templateIndices = new Map<string, number>();

		const transformedAgents: Agent[] = data.areas.flatMap((area) => {
			return area.agents
				.filter((agent) => agent.count > 0)
				.flatMap((agent) => {
					const template = agentTemplates.find(
						(t) => t.name === agent.templateId
					);
					if (!template) return [];

					const totalForTemplate =
						templateCounts.get(template.name) || 0;
					const currentIndex =
						templateIndices.get(template.name) || 1;

					return Array.from({ length: agent.count }, (_, i) => {
						const agentIndex = currentIndex + i;
						const aid =
							totalForTemplate === 1
								? template.name
								: `${template.name} ${agentIndex}`;

						templateIndices.set(template.name, agentIndex + 1);

						return {
							aid,
							area: area.name,
							name: template.name,
							type: template.type,
							status: {
								isEnabled: true,
								isWorking: false,
							} as AgentStatus,
						};
					});
				});
		});

		// Update the store
		addConfiguration(transformedAreas, transformedAgents);

		console.log("Form submitted with data:", data);
		console.log("Transformed areas:", transformedAreas);
		console.log("Transformed agents:", transformedAgents);
	}

	return (
		<form id="configuration-form" onSubmit={form.handleSubmit(onSubmit)}>
			<ScrollArea className="h-[calc(75dvh)]">
				<div className="mr-4 flex flex-col gap-4">
					<div className="flex gap-4 justify-between">
						<FormField
							control={form.control}
							name="simulationStartTime"
							render={({ field }) => (
								<FormItem>
									<FormLabel>Simulation Start Time</FormLabel>
									<FormControl>
										<TimePicker
											date={field.value}
											setDate={field.onChange}
											{...field}
										/>
									</FormControl>
									<FormMessage />
								</FormItem>
							)}
						/>
						<FormField
							control={form.control}
							name="maxPowerCapacity"
							render={({ field }) => (
								<FormItem>
									<FormLabel>
										Max Power Capacity (W)
									</FormLabel>
									<FormControl>
										<Input
											type="number"
											min={0}
											step={1}
											{...field}
											value={field.value.toString()}
											onChange={(e) =>
												field.onChange(
													Number(e.target.value)
												)
											}
										/>
									</FormControl>
									<FormMessage />
								</FormItem>
							)}
						/>
						<FormField
							control={form.control}
							name="credits"
							render={({ field }) => (
								<FormItem>
									<FormLabel>Credits</FormLabel>
									<FormControl>
										<Input
											type="number"
											min={0}
											step={1}
											{...field}
											value={field.value.toString()}
											onChange={(e) =>
												field.onChange(
													Number(e.target.value)
												)
											}
										/>
									</FormControl>
									<FormMessage />
								</FormItem>
							)}
						/>
					</div>
					{areas.map((field, areaIndex) => {
						return (
							<Card key={field.id}>
								<CardHeader>
									<CardTitle>
										<FormField
											control={form.control}
											name={`areas.${areaIndex}.name`}
											render={({ field }) => (
												<FormItem>
													<FormLabel>
														Room Name
													</FormLabel>
													<FormControl>
														<Input
															placeholder="Living Room"
															{...field}
														/>
													</FormControl>
													<FormMessage />
												</FormItem>
											)}
										/>
									</CardTitle>
									<CardDescription>
										Configure agents for this area
									</CardDescription>
								</CardHeader>
								<CardContent className="flex flex-col gap-4">
									<FormField
										key={field.id}
										control={form.control}
										name={`areas.${areaIndex}.attributes.temperature`}
										render={({ field }) => (
											<FormItem>
												<FormLabel>
													Temperature
												</FormLabel>
												<FormDescription>
													Set the default temperature
													for this area.
												</FormDescription>
												<FormControl>
													<CounterInput
														value={field.value}
														onValueChange={
															field.onChange
														}
														min={0}
														step={0.1}
														format={(value) =>
															`${value} °C`
														}
													/>
												</FormControl>
											</FormItem>
										)}
									/>
									<FormField
										control={form.control}
										name={`areas.${areaIndex}.agents`}
										render={({ field }) => (
											<FormItem>
												<FormLabel>Agents</FormLabel>
												<FormDescription>
													Select agents for this area
													(at least one required)
												</FormDescription>
												<FormControl>
													<ConfigurationFormAgents
														areaIndex={areaIndex}
														{...field}
													/>
												</FormControl>
												<FormMessage />
											</FormItem>
										)}
									/>
								</CardContent>
								<CardFooter>
									{areas.length > 1 && (
										<Button
											type="button"
											size="icon"
											variant="ghost"
											onClick={() =>
												fieldArray.remove(areaIndex)
											}
										>
											<X />
										</Button>
									)}
								</CardFooter>
							</Card>
						);
					})}
				</div>
			</ScrollArea>
		</form>
	);
};

export default ConfigurationForm;
